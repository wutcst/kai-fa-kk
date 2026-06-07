/**
 * 该类是“芝麻开门”后端的游戏会话服务。
 * 游戏会话服务负责创建初始地图、初始化玩家，并向前端提供当前游戏状态。
 *
 * GameService 当前实现开始游戏、读取状态、移动、返回、拾取、丢弃、使用物品、门票扣费和体力消耗规则。
 * 救援、暗语、登录、SQLite 存档和通关规则会在后续 Issue 中继续基于当前会话模型扩展。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.service;

import cn.edu.whut.sept.sesame.dto.AuthResult;
import cn.edu.whut.sept.sesame.dto.GameSaveSummary;
import cn.edu.whut.sept.sesame.dto.GameState;
import cn.edu.whut.sept.sesame.model.GameSession;
import cn.edu.whut.sept.sesame.model.GameStatus;
import cn.edu.whut.sept.sesame.model.Item;
import cn.edu.whut.sept.sesame.model.ItemType;
import cn.edu.whut.sept.sesame.model.Player;
import cn.edu.whut.sept.sesame.model.Room;
import cn.edu.whut.sept.sesame.persistence.GameSaveRecord;
import cn.edu.whut.sept.sesame.persistence.SqliteGameStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 提供游戏开始、状态读取、移动、物品操作、暗语、救援、通关和存档能力的业务服务。
 */
@Service
public class GameService {

    /**
     * 玩家初始所在房间编号。
     */
    public static final String START_ROOM_ID = "entrance";

    /**
     * 第 2 关开始房间编号。
     */
    public static final String LEVEL_TWO_START_ROOM_ID = "moon-corridor";

    /**
     * 第 3 关开始房间编号。
     */
    public static final String LEVEL_THREE_START_ROOM_ID = "throne-antechamber";

    /**
     * 玩家进入秘窟需要支付的入场费。
     */
    public static final int TICKET_COST = 10;

    /**
     * 玩家每次合法移动需要消耗的体力。
     */
    public static final int MOVE_STAMINA_COST = 5;

    /**
     * 商店中无法购买物品时使用的价格。
     */
    public static final int UNKNOWN_PRICE = -1;

    /**
     * 解锁宝库和最终出口所需的正确暗语。
     */
    public static final String CORRECT_PASSWORD = "芝麻开门";

    private final ConcurrentMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> loginTokens = new ConcurrentHashMap<>();
    private final SqliteGameStore gameStore;

    /**
     * 创建默认游戏服务。
     */
    public GameService() {
        this(new SqliteGameStore());
    }

    /**
     * 使用指定 SQLite 存储组件创建游戏服务。
     *
     * @param gameStore SQLite 存储组件
     */
    @Autowired
    public GameService(SqliteGameStore gameStore) {
        this.gameStore = Objects.requireNonNull(gameStore, "SQLite 存储组件不能为空");
    }

    /**
     * 注册玩家账号。
     *
     * @param username 用户名
     * @param password 密码
     * @return 账号操作结果
     */
    public AuthResult register(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        String passwordHash = hashPassword(requireText(password, "密码不能为空"));
        boolean success = gameStore.registerUser(normalizedUsername, passwordHash);
        if (!success) {
            return new AuthResult(normalizedUsername, false, "用户名已存在。", null);
        }
        return new AuthResult(normalizedUsername, true, "注册成功，请继续登录。", null);
    }

    /**
     * 登录玩家账号。
     *
     * @param username 用户名
     * @param password 密码
     * @return 账号操作结果
     */
    public AuthResult login(String username, String password) {
        String normalizedUsername = normalizeUsername(username);
        String passwordHash = hashPassword(requireText(password, "密码不能为空"));
        boolean matched = gameStore.findPasswordHash(normalizedUsername)
                .map(passwordHash::equals)
                .orElse(false);
        if (!matched) {
            return new AuthResult(normalizedUsername, false, "用户名或密码错误。", null);
        }
        String token = createLoginToken();
        loginTokens.put(token, normalizedUsername);
        return new AuthResult(normalizedUsername, true, "登录成功。", token);
    }

    /**
     * 开始一局新游戏，并返回初始游戏状态。
     *
     * @return 初始游戏状态
     */
    public GameState startGame() {
        return startGame(createSessionId());
    }

    /**
     * 使用指定会话编号开始一局新游戏，并返回初始游戏状态。
     *
     * @param sessionId 会话编号
     * @return 初始游戏状态
     */
    public GameState startGame(String sessionId) {
        return startGame(sessionId, Player.DEFAULT_MONEY);
    }

    /**
     * 使用指定会话编号和初始金额开始一局新游戏，并返回初始游戏状态。
     *
     * @param sessionId 会话编号
     * @param initialMoney 初始金额
     * @return 初始游戏状态
     */
    public GameState startGame(String sessionId, int initialMoney) {
        GameSession session = createInitialSession(sessionId, initialMoney, "");
        if (!session.getPlayer().pay(TICKET_COST)) {
            session.setStatus(GameStatus.FAILED);
            session.addLog("金额不足，无法支付入场费。");
            sessions.put(session.getId(), session);
            return GameState.from(session, "金额不足，无法开始探索。");
        }

        session.markLevelCheckpoint();
        session.addLog("你支付了 " + TICKET_COST + " 金币入场费。");
        session.addLog("你站在秘窟入口，前方是连通三片遗迹区域的地下秘窟。");
        session.addLog("游戏目标：穿过三片区域，合理保留或出售宝物，最终用金币结算积分。");
        sessions.put(session.getId(), session);
        return GameState.from(session, "游戏已开始。");
    }

    /**
     * 使用登录令牌开始一局新游戏，并返回初始游戏状态。
     *
     * @param token 登录令牌
     * @return 初始游戏状态
     */
    public GameState startGameWithToken(String token) {
        String username = requireUsernameByToken(token);
        GameState state = startGame(createSessionId(), Player.DEFAULT_MONEY, username);
        return state;
    }

    private GameState startGame(String sessionId, int initialMoney, String username) {
        GameSession session = createInitialSession(sessionId, initialMoney, username);
        if (!session.getPlayer().pay(TICKET_COST)) {
            session.setStatus(GameStatus.FAILED);
            session.addLog("金额不足，无法支付入场费。");
            sessions.put(session.getId(), session);
            return GameState.from(session, "金额不足，无法开始探索。");
        }

        session.markLevelCheckpoint();
        session.addLog("你支付了 " + TICKET_COST + " 金币入场费。");
        session.addLog("你站在秘窟入口，前方是连通三片遗迹区域的地下秘窟。");
        session.addLog("游戏目标：穿过三片区域，合理保留或出售宝物，最终用金币结算积分。");
        sessions.put(session.getId(), session);
        return GameState.from(session, "游戏已开始。");
    }

    /**
     * 获取当前游戏状态。
     *
     * @param sessionId 会话编号
     * @return 当前游戏状态
     */
    public GameState getState(String sessionId) {
        GameSession session = requireSession(sessionId);
        return GameState.from(session, "当前游戏状态已刷新。");
    }

    /**
     * 按指定方向移动玩家，并返回移动后的游戏状态。
     *
     * @param sessionId 会话编号
     * @param direction 移动方向
     * @return 移动后的游戏状态
     */
    public GameState move(String sessionId, String direction) {
        GameSession session = requireSession(sessionId);
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return GameState.from(session, "当前不在探索状态，不能移动。");
        }

        Player player = session.getPlayer();
        Room currentRoom = session.getCurrentRoom();
        String normalizedDirection = normalizeDirection(direction);
        String targetRoomId = currentRoom.getExit(normalizedDirection).orElse(null);
        if (targetRoomId == null) {
            String message = "当前房间没有通向 " + normalizedDirection + " 的出口。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        Room targetRoom = session.findRoom(targetRoomId)
                .orElseThrow(() -> new IllegalStateException("目标房间不存在：" + targetRoomId));
        if (targetRoom.isRequiresPassword() && !session.isPasswordUnlocked()) {
            String message = targetRoom.getName() + "需要正确暗语才能进入。";
            session.addLog(message);
            return GameState.from(session, message);
        }
        String requiredItemId = requiredItemForRoom(targetRoomId);
        if (requiredItemId != null && !player.hasItem(requiredItemId)) {
            String message = targetRoom.getName() + "被特殊机关封住，似乎需要 "
                    + createItem(requiredItemId).getName() + " 才能进入。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        if (player.getStamina() < MOVE_STAMINA_COST) {
            return restartCurrentLevel(session, "体力不足，当前关重新开始。");
        }

        player.decreaseStamina(MOVE_STAMINA_COST);
        session.setPreviousRoomId(currentRoom.getId());
        player.moveTo(targetRoomId);
        String message = "你向 " + normalizedDirection + " 移动，进入了" + targetRoom.getName() + "。";
        session.addLog(message + " 消耗体力 " + MOVE_STAMINA_COST + "。");
        if (player.getStamina() == 0) {
            return restartCurrentLevel(session, "体力耗尽，当前关重新开始。");
        }
        checkRoomTransition(session);
        return GameState.from(session, message);
    }

    /**
     * 返回玩家上一次所在房间，并返回返回后的游戏状态。
     *
     * @param sessionId 会话编号
     * @return 返回后的游戏状态
     */
    public GameState back(String sessionId) {
        GameSession session = requireSession(sessionId);
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return GameState.from(session, "当前不在探索状态，不能返回上一房间。");
        }

        String previousRoomId = session.getPreviousRoomId().orElse(null);
        if (previousRoomId == null) {
            String message = "当前还没有可以返回的上一个房间。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        Player player = session.getPlayer();
        if (player.getStamina() < MOVE_STAMINA_COST) {
            return restartCurrentLevel(session, "体力不足，当前关重新开始。");
        }

        Room currentRoom = session.getCurrentRoom();
        Room previousRoom = session.findRoom(previousRoomId)
                .orElseThrow(() -> new IllegalStateException("上一个房间不存在：" + previousRoomId));
        player.decreaseStamina(MOVE_STAMINA_COST);
        session.setPreviousRoomId(currentRoom.getId());
        player.moveTo(previousRoom.getId());
        String message = "你返回了" + previousRoom.getName() + "。";
        session.addLog(message + " 消耗体力 " + MOVE_STAMINA_COST + "。");
        if (player.getStamina() == 0) {
            return restartCurrentLevel(session, "体力耗尽，当前关重新开始。");
        }
        checkRoomTransition(session);
        return GameState.from(session, message);
    }

    /**
     * 从当前房间拾取指定物品，并返回拾取后的游戏状态。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 拾取后的游戏状态
     */
    public GameState takeItem(String sessionId, String itemId) {
        GameSession session = requireSession(sessionId);
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return GameState.from(session, "游戏已经结束，不能拾取物品。");
        }

        String normalizedItemId = requireText(itemId, "物品编号不能为空");
        Room currentRoom = session.getCurrentRoom();
        Item item = currentRoom.findItem(normalizedItemId).orElse(null);
        if (item == null) {
            String message = "当前房间没有该物品：" + normalizedItemId + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        Player player = session.getPlayer();
        if (!player.canCarry(item)) {
            String message = "背包负重不足，无法拾取" + item.getName() + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        Item removedItem = currentRoom.removeItem(normalizedItemId)
                .orElseThrow(() -> new IllegalStateException("房间物品状态不一致：" + normalizedItemId));
        player.addItem(removedItem);
        String message = "你拾取了" + removedItem.getName() + "。";
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 从背包丢弃指定物品到当前房间，并返回丢弃后的游戏状态。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 丢弃后的游戏状态
     */
    public GameState dropItem(String sessionId, String itemId) {
        GameSession session = requireSession(sessionId);
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return GameState.from(session, "游戏已经结束，不能丢弃物品。");
        }

        String normalizedItemId = requireText(itemId, "物品编号不能为空");
        Player player = session.getPlayer();
        Item item = player.removeItem(normalizedItemId).orElse(null);
        if (item == null) {
            String message = "背包中没有该物品：" + normalizedItemId + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        session.getCurrentRoom().addItem(item);
        String message = "你丢弃了" + item.getName() + "。";
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 使用背包中的指定物品，并返回使用后的游戏状态。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 使用后的游戏状态
     */
    public GameState useItem(String sessionId, String itemId) {
        GameSession session = requireSession(sessionId);
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return GameState.from(session, "游戏已经结束，不能使用物品。");
        }

        String normalizedItemId = requireText(itemId, "物品编号不能为空");
        Player player = session.getPlayer();
        Item item = player.getInventory().stream()
                .filter(candidate -> candidate.getId().equals(normalizedItemId))
                .findFirst()
                .orElse(null);
        if (item == null) {
            String message = "背包中没有该物品：" + normalizedItemId + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        if (item.getType() != ItemType.SUPPLY && item.getType() != ItemType.EQUIPMENT) {
            String message = item.getName() + "暂时不能直接使用。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        player.removeItem(normalizedItemId);
        if (item.getStaminaEffect() > 0) {
            player.restoreStamina(item.getStaminaEffect());
        }
        if (item.getMaxWeightEffect() > 0) {
            player.increaseMaxWeight(item.getMaxWeightEffect());
        }

        String message = buildUseItemMessage(item);
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 提交暗语，正确时解锁宝库和最终出口。
     *
     * @param sessionId 会话编号
     * @param password 玩家输入的暗语
     * @return 提交暗语后的游戏状态
     */
    public GameState submitPassword(String sessionId, String password) {
        GameSession session = requireSession(sessionId);
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return GameState.from(session, "游戏已经结束，不能继续输入暗语。");
        }
        if (!"mechanism-gallery".equals(session.getPlayer().getCurrentRoomId())) {
            String message = "这里没有可以输入暗语的机关。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        String normalizedPassword = requireText(password, "暗语不能为空").trim();
        if (!CORRECT_PASSWORD.equals(normalizedPassword)) {
            String message = "暗语错误，石门没有任何反应。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        session.setPasswordUnlocked(true);
        String message = "暗语正确，最终石门的纹路亮了起来。";
        session.addLog(message);
        checkRoomTransition(session);
        return GameState.from(session, message);
    }

    /**
     * 将当前游戏会话保存到 SQLite。
     *
     * @param username 用户名
     * @param sessionId 会话编号
     * @return 保存后的游戏状态
     */
    public GameState saveGame(String token, String sessionId) {
        return saveGame(token, sessionId, "自动存档");
    }

    /**
     * 将当前游戏会话保存到 SQLite。
     *
     * @param token 登录令牌
     * @param sessionId 会话编号
     * @param saveName 存档名称
     * @return 保存后的游戏状态
     */
    public GameState saveGame(String token, String sessionId, String saveName) {
        String normalizedUsername = requireUsernameByToken(token);
        GameSession session = requireSession(sessionId);
        if (!normalizedUsername.equals(session.getUsername())) {
            throw new IllegalStateException("不能保存其他玩家的游戏会话。");
        }
        long saveId = gameStore.saveGame(toSaveRecord(normalizedUsername, session, saveName));
        session.setCurrentSaveId(saveId);
        String message = "游戏已保存到 SQLite，存档编号：" + saveId + "。";
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 从 SQLite 读取指定用户的游戏存档。
     *
     * @param username 用户名
     * @return 读取后的游戏状态
     */
    public GameState loadGame(String token) {
        String normalizedUsername = requireUsernameByToken(token);
        GameSaveRecord record = gameStore.findLatestGameSave(normalizedUsername)
                .orElseThrow(() -> new IllegalStateException("该用户没有可读取的游戏存档。"));
        GameSession session = fromSaveRecord(record);
        sessions.put(session.getId(), session);
        String message = "已从 SQLite 读取游戏存档。";
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 按存档编号读取指定用户的游戏存档。
     *
     * @param token 登录令牌
     * @param saveId 存档编号
     * @return 读取后的游戏状态
     */
    public GameState loadGame(String token, long saveId) {
        String normalizedUsername = requireUsernameByToken(token);
        GameSaveRecord record = gameStore.findGameSave(normalizedUsername, saveId)
                .orElseThrow(() -> new IllegalStateException("找不到指定存档：" + saveId));
        GameSession session = fromSaveRecord(record);
        sessions.put(session.getId(), session);
        String message = "已读取存档：" + record.getSaveName() + "。";
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 查询当前登录用户的存档列表。
     *
     * @param token 登录令牌
     * @return 存档摘要列表
     */
    public List<GameSaveSummary> listGameSaves(String token) {
        String normalizedUsername = requireUsernameByToken(token);
        return gameStore.listGameSaves(normalizedUsername);
    }

    /**
     * 在商店购买指定物品。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 购买后的游戏状态
     */
    public GameState buyItem(String sessionId, String itemId) {
        GameSession session = requireShoppingSession(sessionId);
        String normalizedItemId = requireText(itemId, "物品编号不能为空");
        int price = shopPrice(normalizedItemId);
        if (price == UNKNOWN_PRICE) {
            String message = "商店没有出售该物品：" + normalizedItemId + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        Player player = session.getPlayer();
        if (!player.pay(price)) {
            String message = "金币不足，无法购买" + createItem(normalizedItemId).getName() + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        Item item = createItem(normalizedItemId);
        if (!player.addItem(item)) {
            player.earn(price);
            String message = "背包负重不足，无法购买" + item.getName() + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        String message = "你花费 " + price + " 金币购买了" + item.getName() + "。";
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 在商店出售背包中的宝物。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 出售后的游戏状态
     */
    public GameState sellItem(String sessionId, String itemId) {
        GameSession session = requireShoppingSession(sessionId);
        String normalizedItemId = requireText(itemId, "物品编号不能为空");
        Player player = session.getPlayer();
        Item item = player.getInventory().stream()
                .filter(candidate -> candidate.getId().equals(normalizedItemId))
                .findFirst()
                .orElse(null);
        if (item == null) {
            String message = "背包中没有该物品：" + normalizedItemId + "。";
            session.addLog(message);
            return GameState.from(session, message);
        }
        if (item.getType() != ItemType.TREASURE) {
            String message = item.getName() + "不是宝物，商店不回收。";
            session.addLog(message);
            return GameState.from(session, message);
        }

        player.removeItem(normalizedItemId);
        player.earn(item.getMoneyValue());
        String message = "你出售了" + item.getName() + "，获得 " + item.getMoneyValue() + " 金币。";
        session.addLog(message);
        return GameState.from(session, message);
    }

    /**
     * 从商店继续进入下一关。
     *
     * @param sessionId 会话编号
     * @return 进入下一关后的游戏状态
     */
    public GameState continueAdventure(String sessionId) {
        GameSession session = requireShoppingSession(sessionId);
        if (session.getCurrentLevel() == 1) {
            enterLevel(session, 2, LEVEL_TWO_START_ROOM_ID, "你离开石门营地，进入月纹回廊。");
            return GameState.from(session, "第 2 关开始。");
        }
        if (session.getCurrentLevel() == 2) {
            enterLevel(session, 3, LEVEL_THREE_START_ROOM_ID, "你离开回廊营地，抵达沉金王座前厅。");
            return GameState.from(session, "第 3 关开始。");
        }
        return GameState.from(session, "已经没有下一关。");
    }

    /**
     * 重新开始当前关。
     *
     * @param sessionId 会话编号
     * @return 重新开始后的游戏状态
     */
    public GameState restartLevel(String sessionId) {
        GameSession session = requireSession(sessionId);
        return restartCurrentLevel(session, "当前关已重新开始。");
    }

    /**
     * 放弃本次探险，删除当前存档并结束会话。
     *
     * @param sessionId 会话编号
     * @return 放弃后的游戏状态
     */
    public GameState abandonAdventure(String sessionId) {
        GameSession session = requireSession(sessionId);
        if (!session.getUsername().isBlank()) {
            session.getCurrentSaveId().ifPresent(saveId -> gameStore.deleteGameSave(session.getUsername(), saveId));
        }
        session.setFinalScore(0);
        session.setStatus(GameStatus.FAILED);
        session.addLog("你放弃了本次探险，当前存档已删除，本次积分为 0。");
        sessions.remove(session.getId());
        return GameState.from(session, "已放弃本次探险。");
    }

    /**
     * 获取当前游戏会话，主要用于服务层测试和后续业务规则复用。
     *
     * @param sessionId 会话编号
     * @return 当前游戏会话
     */
    GameSession getCurrentSession(String sessionId) {
        return requireSession(sessionId);
    }

    private GameSession createInitialSession(String sessionId, int initialMoney, String username) {
        Map<String, Room> rooms = createRooms();
        Player player = new Player(START_ROOM_ID, initialMoney, Player.DEFAULT_STAMINA,
                Player.DEFAULT_STAMINA, Player.DEFAULT_MAX_WEIGHT);
        GameSession session = new GameSession(sessionId, username, player, rooms, GameStatus.IN_PROGRESS);
        session.setCurrentLevel(1);
        session.setHighScore(username.isBlank() ? 0 : gameStore.findHighScore(username));
        session.markLevelCheckpoint();
        return session;
    }

    private Map<String, Room> createRooms() {
        Map<String, Room> rooms = new LinkedHashMap<>();

        Room entrance = new Room(START_ROOM_ID, "秘窟入口", "石阶向地下延伸，潮湿墙面上刻着模糊的芝麻纹。");
        Room outerCourt = new Room("stone-court", "石门外庭", "外庭中央倒伏着半截石柱，地面铺着月牙形碎砖。");
        Room altar = new Room("old-altar", "旧祭坛", "祭坛上覆着厚灰，凹槽形状像一轮残月。");
        Room bridge = new Room("broken-bridge", "断裂石桥", "石桥横跨幽暗裂隙，桥面还能看见拖拽宝箱的痕迹。");
        Room moonChamber = new Room("bronze-moon-room", "铜月石室", "石室门楣刻着铜月纹，角落里有被撬开的旧箱。");
        Room supplyAlcove = new Room("supply-alcove", "补给壁龛", "壁龛里残留着前人留下的补给。");
        Room sidePath = new Room("mechanism-path", "机关侧道", "侧道墙里传来齿轮声，尽头是一扇厚重关门。");
        Room stoneGate = new Room("stone-gate", "石门关门", "第一片遗迹的关门已经半开，门后有商队营火的光。");
        Room stoneCamp = new Room("stone-camp", "石门营地", "古商人占据的安全营地，可以在这里卖宝物和购买道具。");

        Room moonCorridor = new Room(LEVEL_TWO_START_ROOM_ID, "月纹回廊", "回廊顶部有月相浮雕，脚步声会被石壁反复送回。");
        Room moonSecret = new Room("moon-secret-room", "月纹密室", "密室石门只回应铜月钥牌，里面藏着更深处的线索。");
        Room hiddenChest = new Room("hidden-chest-room", "隐藏宝箱", "小室中央放着一只贴满月纹封条的宝箱。");
        Room sandPit = new Room("sand-pit", "流沙陷坑", "细沙从石缝里不断涌出，稍不留神就会拖慢脚步。");
        Room echoHall = new Room("echo-hall", "回声石厅", "石厅空旷，远处传来的水声像人在低语。");
        Room merchantBones = new Room("merchant-bones", "商旅遗骨", "散落的商旅遗骨旁压着一枚古旧印章。");
        Room drainPath = new Room("drain-path", "暗渠小径", "低矮暗渠勉强能通过，水面漂着细碎金砂。");
        Room starAltar = new Room("star-altar", "星纹祭台", "祭台中央的星砂罗盘仍在缓慢转动。");
        Room moonGate = new Room("moon-gate", "月纹关门", "第二片遗迹尽头的关门，门后能闻到商店灯油味。");
        Room corridorCamp = new Room("corridor-camp", "回廊营地", "商人把货箱堆在回廊尽头，等待进入王座区的探险者。");

        Room throneAntechamber = new Room(LEVEL_THREE_START_ROOM_ID, "王座前厅", "前厅铺着破碎金砖，空气比前两片区域更加沉重。");
        Room starSideHall = new Room("star-side-hall", "星纹侧殿", "侧殿入口的星纹机关只回应星纹罗盘。");
        Room ancientCoffer = new Room("ancient-coffer", "古代宝匣", "宝匣被星砂封住，里面似乎藏着王权遗物。");
        Room throne = new Room("golden-throne", "沉金王座", "王座沉在碎金与尘土之间，扶手上刻着古王名。");
        Room longHall = new Room("mechanism-gallery", "机关长廊", "长廊墙面布满机关孔，脚下石板一块比一块沉。");
        Room finalGate = new Room("final-gate", "最终石门", "最后的石门紧闭，门上只留下等待暗语的芝麻纹路。", true, true);
        Room brokenVault = new Room("broken-vault", "破碎金库", "金库已经坍塌一半，仍有少量宝物埋在碎石间。");
        Room deepWell = new Room("deep-well-altar", "深井祭坛", "祭坛下方是看不见底的深井，井壁镶着黑色珍珠。");
        Room hiddenVault = new Room("hidden-vault", "隐秘宝库", "宝库藏在深井祭坛后方，墙上没有任何外人来过的痕迹。");

        connectRooms(entrance, "east", outerCourt);
        connectRooms(outerCourt, "north", altar);
        connectRooms(outerCourt, "east", bridge);
        connectRooms(outerCourt, "south", supplyAlcove);
        connectRooms(bridge, "east", moonChamber);
        connectRooms(bridge, "south", sidePath);
        connectRooms(sidePath, "south", stoneGate);
        connectRooms(stoneGate, "south", stoneCamp);
        connectRooms(stoneCamp, "south", moonCorridor);

        connectRooms(moonCorridor, "west", moonSecret);
        connectRooms(moonSecret, "south", hiddenChest);
        connectRooms(moonCorridor, "east", sandPit);
        connectRooms(moonCorridor, "south", merchantBones);
        connectRooms(sandPit, "east", echoHall);
        connectRooms(sandPit, "south", drainPath);
        connectRooms(drainPath, "south", starAltar);
        connectRooms(echoHall, "south", moonGate);
        connectRooms(moonGate, "south", corridorCamp);
        connectRooms(corridorCamp, "south", throneAntechamber);

        connectRooms(throneAntechamber, "west", starSideHall);
        connectRooms(starSideHall, "south", ancientCoffer);
        connectRooms(throneAntechamber, "east", throne);
        connectRooms(throneAntechamber, "south", brokenVault);
        connectRooms(throne, "east", longHall);
        connectRooms(longHall, "east", finalGate);
        connectRooms(longHall, "south", deepWell);
        connectRooms(deepWell, "south", hiddenVault);

        entrance.addItem(createItem("old-map"));
        supplyAlcove.addItem(createItem("clean-water"));
        supplyAlcove.addItem(createItem("dry-food"));
        altar.addItem(createItem("silver-cup"));
        moonChamber.addItem(createItem("bronze-moon-token"));
        stoneGate.addItem(createItem("jade-bell"));

        moonSecret.addItem(createItem("moon-dial"));
        hiddenChest.addItem(createItem("sandglass"));
        merchantBones.addItem(createItem("merchant-seal"));
        starAltar.addItem(createItem("star-compass"));

        brokenVault.addItem(createItem("ruby-idol"));
        throne.addItem(createItem("gold-crown"));
        ancientCoffer.addItem(createItem("king-scepter"));
        hiddenVault.addItem(createItem("black-pearl"));

        for (Room room : List.of(entrance, outerCourt, altar, bridge, moonChamber, supplyAlcove, sidePath,
                stoneGate, stoneCamp, moonCorridor, moonSecret, hiddenChest, sandPit, echoHall, merchantBones,
                drainPath, starAltar, moonGate, corridorCamp, throneAntechamber, starSideHall, ancientCoffer,
                throne, longHall, finalGate, brokenVault, deepWell, hiddenVault)) {
            rooms.put(room.getId(), room);
        }

        return rooms;
    }

    private GameState restartCurrentLevel(GameSession session, String reason) {
        GameSession restarted = createSessionFromCheckpoint(session, reason);
        sessions.put(restarted.getId(), restarted);
        return GameState.from(restarted, reason);
    }

    private GameSession createSessionFromCheckpoint(GameSession session, String reason) {
        Player player = new Player(session.getLevelStartRoomId(), session.getLevelStartMoney(),
                Player.DEFAULT_STAMINA, Player.DEFAULT_STAMINA, session.getLevelStartMaxWeight());
        for (String itemId : session.getLevelStartInventoryItems()) {
            player.addItem(createItem(itemId));
        }
        GameSession restarted = new GameSession(session.getId(), session.getUsername(), player, createRooms(),
                GameStatus.IN_PROGRESS);
        restarted.setCurrentLevel(session.getCurrentLevel());
        restarted.setPasswordUnlocked(session.isPasswordUnlocked());
        restarted.setHighScore(session.getHighScore());
        restarted.setLevelCheckpoint(session.getLevelStartRoomId(), session.getLevelStartMoney(),
                session.getLevelStartMaxWeight(), session.getLevelStartInventoryItems());
        restarted.addLog(reason);
        return restarted;
    }

    private void checkRoomTransition(GameSession session) {
        String currentRoomId = session.getPlayer().getCurrentRoomId();
        if ("stone-camp".equals(currentRoomId)) {
            session.setStatus(GameStatus.SHOPPING);
            session.addLog("你抵达石门营地，可以出售宝物或购买道具。");
            return;
        }
        if ("corridor-camp".equals(currentRoomId)) {
            session.setStatus(GameStatus.SHOPPING);
            session.addLog("你抵达回廊营地，可以在进入王座区前整理物资。");
            return;
        }
        if ("final-gate".equals(currentRoomId) && session.isPasswordUnlocked()) {
            finishAdventure(session);
        }
    }

    private void finishAdventure(GameSession session) {
        Player player = session.getPlayer();
        int treasureValue = player.getInventory().stream()
                .filter(item -> item.getType() == ItemType.TREASURE)
                .mapToInt(Item::getMoneyValue)
                .sum();
        List<String> treasureIds = player.getInventory().stream()
                .filter(item -> item.getType() == ItemType.TREASURE)
                .map(Item::getId)
                .collect(Collectors.toList());
        for (String treasureId : treasureIds) {
            player.removeItem(treasureId);
        }
        player.earn(treasureValue);
        int finalScore = player.getMoney();
        session.setFinalScore(finalScore);
        if (!session.getUsername().isBlank()) {
            session.setHighScore(gameStore.updateHighScore(session.getUsername(), finalScore));
            session.getCurrentSaveId().ifPresent(saveId -> gameStore.deleteGameSave(session.getUsername(), saveId));
        }
        session.setStatus(GameStatus.WON);
        session.addLog("最终石门开启，剩余宝物自动卖出 " + treasureValue + " 金币。");
        session.addLog("本次最终积分为 " + finalScore + "，当前历史最高分为 " + session.getHighScore() + "。");
    }

    private GameSession requireShoppingSession(String sessionId) {
        GameSession session = requireSession(sessionId);
        if (session.getStatus() != GameStatus.SHOPPING) {
            throw new IllegalStateException("当前不在商店阶段。");
        }
        return session;
    }

    private void enterLevel(GameSession session, int level, String startRoomId, String log) {
        session.setCurrentLevel(level);
        session.setStatus(GameStatus.IN_PROGRESS);
        session.getPlayer().moveTo(startRoomId);
        session.getPlayer().restoreStamina(session.getPlayer().getMaxStamina());
        session.clearPreviousRoomId();
        session.markLevelCheckpoint();
        session.addLog(log);
    }

    private String requiredItemForRoom(String roomId) {
        if ("moon-secret-room".equals(roomId)) {
            return "bronze-moon-token";
        }
        if ("star-side-hall".equals(roomId)) {
            return "star-compass";
        }
        return null;
    }

    private int shopPrice(String itemId) {
        switch (itemId) {
            case "clean-water":
                return 10;
            case "dry-food":
                return 8;
            case "stamina-potion":
                return 18;
            case "rope":
                return 20;
            case "iron-boots":
                return 25;
            case "lockpick":
                return 25;
            case "lantern":
                return 15;
            default:
                return UNKNOWN_PRICE;
        }
    }

    private GameSaveRecord toSaveRecord(String username, GameSession session, String saveName) {
        Player player = session.getPlayer();
        return new GameSaveRecord(username, session.getCurrentSaveId().orElse(null),
                normalizeSaveName(saveName), session.getId(), player.getCurrentRoomId(),
                session.getPreviousRoomId().orElse(null), player.getMoney(), player.getStamina(),
                player.getMaxStamina(), player.getMaxWeight(), session.getCurrentLevel(),
                session.getLevelStartRoomId(), session.getLevelStartMoney(), session.getLevelStartMaxWeight(),
                String.join(",", session.getLevelStartInventoryItems()), session.getFinalScore(),
                session.getStatus(), session.isPasswordUnlocked(), encodeInventory(player), encodeRoomItems(session),
                encodeLogs(session));
    }

    private GameSession fromSaveRecord(GameSaveRecord record) {
        Map<String, Room> rooms = createRooms();
        clearRoomItems(rooms);
        restoreRoomItems(rooms, record.getRoomItems());
        Player player = new Player(record.getCurrentRoomId(), record.getMoney(), record.getStamina(),
                record.getMaxStamina(), record.getMaxWeight());
        for (String itemId : splitList(record.getInventoryItems())) {
            player.addItem(createItem(itemId));
        }
        GameSession session = new GameSession(record.getSessionId(), record.getUsername(), player, rooms,
                record.getStatus());
        session.setCurrentSaveId(record.getSaveId());
        session.setCurrentLevel(record.getCurrentLevel());
        session.setFinalScore(record.getFinalScore());
        session.setHighScore(gameStore.findHighScore(record.getUsername()));
        session.setLevelCheckpoint(record.getLevelStartRoomId(), record.getLevelStartMoney(),
                record.getLevelStartMaxWeight(), splitList(record.getLevelStartInventoryItems()));
        if (record.getPreviousRoomId() != null && !record.getPreviousRoomId().isBlank()) {
            session.setPreviousRoomId(record.getPreviousRoomId());
        }
        session.setPasswordUnlocked(record.isPasswordUnlocked());
        for (String log : splitLogs(record.getLogs())) {
            session.addLog(log);
        }
        return session;
    }

    private String encodeInventory(Player player) {
        return player.getInventory().stream().map(Item::getId).collect(Collectors.joining(","));
    }

    private String encodeRoomItems(GameSession session) {
        return session.getRooms().values().stream()
                .map(room -> room.getId() + ":" + room.getItems().stream()
                        .map(Item::getId)
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining("|"));
    }

    private String encodeLogs(GameSession session) {
        return String.join("\n", session.getLogs());
    }

    private void clearRoomItems(Map<String, Room> rooms) {
        for (Room room : rooms.values()) {
            List<String> itemIds = room.getItems().stream().map(Item::getId).collect(Collectors.toList());
            for (String itemId : itemIds) {
                room.removeItem(itemId);
            }
        }
    }

    private void restoreRoomItems(Map<String, Room> rooms, String encodedRoomItems) {
        if (encodedRoomItems == null || encodedRoomItems.isBlank()) {
            return;
        }
        for (String roomPart : encodedRoomItems.split("\\|")) {
            String[] roomAndItems = roomPart.split(":", 2);
            if (roomAndItems.length < 2 || roomAndItems[1].isBlank()) {
                continue;
            }
            Room room = rooms.get(roomAndItems[0]);
            if (room == null) {
                continue;
            }
            for (String itemId : splitList(roomAndItems[1])) {
                room.addItem(createItem(itemId));
            }
        }
    }

    private List<String> splitList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }

    private List<String> splitLogs(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\n"))
                .filter(log -> !log.isBlank())
                .collect(Collectors.toList());
    }

    private Item createItem(String itemId) {
        switch (itemId) {
            case "old-map":
                return new Item("old-map", "残旧地图", "标记着秘窟大致结构的羊皮纸，边角已经被潮气泡烂。", ItemType.KEY, 1, 0, 0, 0);
            case "clean-water":
                return new Item("clean-water", "清水", "装在旧皮囊里的清水，入口微凉，能让探险者短暂恢复精神。", ItemType.SUPPLY, 2, 10, 0, 0);
            case "dry-food":
                return new Item("dry-food", "干粮", "压得很实的谷物饼，味道普通，但足够支撑一段路。", ItemType.SUPPLY, 1, 6, 0, 0);
            case "stamina-potion":
                return new Item("stamina-potion", "提神药剂", "带有草药气味的药剂，能快速恢复体力。", ItemType.SUPPLY, 1, 20, 0, 0);
            case "rope":
                return new Item("rope", "结实绳索", "粗麻编成的绳索，可以把更多物品牢牢捆在背包外侧。", ItemType.EQUIPMENT, 3, 0, 5, 0);
            case "iron-boots":
                return new Item("iron-boots", "铁底靴", "靴底嵌着薄铁片，踩过松动石板时更稳。", ItemType.EQUIPMENT, 4, 0, 0, 0);
            case "lockpick":
                return new Item("lockpick", "开锁针", "一套细小金属针，只能处理普通机关锁。", ItemType.KEY, 1, 0, 0, 0);
            case "lantern":
                return new Item("lantern", "油灯", "灯芯燃起后，墙上的浅刻纹路会变得更清楚。", ItemType.KEY, 2, 0, 0, 0);
            case "bronze-moon-token":
                return new Item("bronze-moon-token", "铜月钥牌",
                        "一枚刻着弯月纹路的铜牌，边缘磨损严重，似乎与某处月纹石门有着些许连接。", ItemType.TREASURE, 2, 0, 0, 60);
            case "jade-bell":
                return new Item("jade-bell", "青玉铃", "小巧的玉铃没有铃舌，却在靠近石门时发出极轻的震动。", ItemType.TREASURE, 2, 0, 0, 45);
            case "silver-cup":
                return new Item("silver-cup", "银纹杯", "杯身刻着蛇形纹路，杯底还残留着干涸的香料痕迹。", ItemType.TREASURE, 2, 0, 0, 35);
            case "moon-dial":
                return new Item("moon-dial", "月影刻盘", "石盘表面刻着月相变化，转动时会发出低沉的摩擦声。", ItemType.TREASURE, 3, 0, 0, 70);
            case "star-compass":
                return new Item("star-compass", "星纹罗盘",
                        "罗盘中央嵌着细小星砂，指针始终偏向地底深处，似乎能回应某座沉睡王座旁的星纹机关。", ItemType.TREASURE, 2, 0, 0, 100);
            case "sandglass":
                return new Item("sandglass", "金砂漏瓶", "透明瓶中流动着细碎金砂，倒转后却不会正常下落。", ItemType.TREASURE, 3, 0, 0, 75);
            case "merchant-seal":
                return new Item("merchant-seal", "商旅印章", "印章上刻着古商队的纹记，商店老板似乎会认得它的来历。", ItemType.TREASURE, 1, 0, 0, 50);
            case "gold-crown":
                return new Item("gold-crown", "沉金王冠", "王冠沉重冰冷，内侧刻着早已失落的王名。", ItemType.TREASURE, 6, 0, 0, 180);
            case "ruby-idol":
                return new Item("ruby-idol", "红宝石神像", "神像双眼嵌着红宝石，火光下像是在注视闯入者。", ItemType.TREASURE, 4, 0, 0, 130);
            case "king-scepter":
                return new Item("king-scepter", "王权短杖", "短杖顶端嵌着暗金星盘，似乎只会回应真正找到星纹道路的人。", ItemType.TREASURE, 5, 0, 0, 220);
            case "black-pearl":
                return new Item("black-pearl", "黑曜珍珠", "珍珠表面没有反光，像是把周围光线全部吞入其中。", ItemType.TREASURE, 3, 0, 0, 160);
            default:
                throw new IllegalArgumentException("未知物品编号：" + itemId);
        }
    }

    private void connectRooms(Room source, String direction, Room target) {
        String normalizedDirection = normalizeDirection(direction);
        if (source.getExit(normalizedDirection).isPresent()) {
            throw new IllegalStateException("重复出口定义：" + source.getId() + " -> " + normalizedDirection);
        }

        String opposite = oppositeDirection(normalizedDirection);
        if (target.getExit(opposite).isPresent()) {
            throw new IllegalStateException("重复出口定义：" + target.getId() + " -> " + opposite);
        }

        source.addExit(normalizedDirection, target.getId());
        target.addExit(opposite, source.getId());
    }

    private String oppositeDirection(String direction) {
        switch (direction) {
            case "north":
                return "south";
            case "south":
                return "north";
            case "east":
                return "west";
            case "west":
                return "east";
            default:
                throw new IllegalArgumentException("未知方向：" + direction);
        }
    }

    private String buildUseItemMessage(Item item) {
        if (item.getType() == ItemType.SUPPLY) {
            return "你使用了" + item.getName() + "，恢复体力 " + item.getStaminaEffect() + "。";
        }
        if (item.getType() == ItemType.EQUIPMENT) {
            return "你使用了" + item.getName() + "，最大负重增加 " + item.getMaxWeightEffect() + "。";
        }
        return "你使用了" + item.getName() + "。";
    }

    private String createSessionId() {
        return "game-session-" + UUID.randomUUID();
    }

    private String createLoginToken() {
        return "login-token-" + UUID.randomUUID();
    }

    private String requireUsernameByToken(String token) {
        String normalizedToken = requireText(token, "登录令牌不能为空");
        String username = loginTokens.get(normalizedToken);
        if (username == null) {
            throw new IllegalStateException("登录状态无效，请先登录。");
        }
        return username;
    }

    private void requireExistingUser(String username) {
        if (gameStore.findPasswordHash(username).isEmpty()) {
            throw new IllegalStateException("用户不存在，请先注册。");
        }
    }

    private GameSession requireSession(String sessionId) {
        String normalizedSessionId = requireText(sessionId, "会话编号不能为空");
        GameSession session = sessions.get(normalizedSessionId);
        if (session == null) {
            throw new IllegalStateException("游戏尚未开始或会话不存在，请先调用 startGame。");
        }
        return session;
    }

    private String normalizeDirection(String direction) {
        return requireText(direction, "方向不能为空").trim().toLowerCase();
    }

    private String normalizeUsername(String username) {
        return requireText(username, "用户名不能为空").trim();
    }

    private String normalizeSaveName(String saveName) {
        if (saveName == null || saveName.isBlank()) {
            return "自动存档";
        }
        return saveName.trim();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(("sesame-open:" + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前环境不支持 SHA-256：" + exception.getMessage(), exception);
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

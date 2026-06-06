/**
 * 该类是“芝麻开门”后端的游戏会话服务。
 * 游戏会话服务负责创建初始地图、初始化玩家，并向前端提供当前游戏状态。
 *
 * GameService 当前只实现开始游戏和读取状态，不处理移动扣体力、门票、背包和暗语规则。
 * 这些规则会在后续 Issue 中继续基于当前会话模型扩展。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.service;

import cn.edu.whut.sept.sesame.dto.GameState;
import cn.edu.whut.sept.sesame.model.GameSession;
import cn.edu.whut.sept.sesame.model.GameStatus;
import cn.edu.whut.sept.sesame.model.Item;
import cn.edu.whut.sept.sesame.model.ItemType;
import cn.edu.whut.sept.sesame.model.Player;
import cn.edu.whut.sept.sesame.model.Room;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

/**
 * 提供游戏开始和状态读取能力的业务服务。
 */
@Service
public class GameService {

    /**
     * 玩家初始所在房间编号。
     */
    public static final String START_ROOM_ID = "entrance";

    private final ConcurrentMap<String, GameSession> sessions = new ConcurrentHashMap<>();

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
        GameSession session = createInitialSession(sessionId);
        session.addLog("你站在秘窟入口，石壁上刻着若隐若现的芝麻纹路。");
        session.addLog("前方传来机关转动的低响，冒险正式开始。");
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
     * 获取当前游戏会话，主要用于服务层测试和后续业务规则复用。
     *
     * @param sessionId 会话编号
     * @return 当前游戏会话
     */
    GameSession getCurrentSession(String sessionId) {
        return requireSession(sessionId);
    }

    private GameSession createInitialSession(String sessionId) {
        Map<String, Room> rooms = createRooms();
        Player player = new Player(START_ROOM_ID);
        return new GameSession(sessionId, player, rooms, GameStatus.IN_PROGRESS);
    }

    private Map<String, Room> createRooms() {
        Map<String, Room> rooms = new LinkedHashMap<>();

        Room entrance = new Room(START_ROOM_ID, "秘窟入口", "潮湿的石阶通向地下，入口处散落着旧火把。");
        Room hall = new Room("stone-hall", "石门大厅", "大厅中央立着巨大的石门，门缝中透出微弱金光。");
        Room supply = new Room("supply-room", "补给洞室", "洞室里堆着旅人留下的补给，空气里有草药味。");
        Room trap = new Room("trap-room", "机关陷阱", "地面铺着松动石板，墙面机关像沉睡的兽眼。");
        Room treasure = new Room("treasure-room", "沉睡宝库", "宝库被古老暗语封印，金色纹路在门上缓慢流动。", false, true);
        Room exit = new Room("final-exit", "最终出口", "出口被最后一道石门挡住，似乎仍需要正确暗语。", true, true);

        connectRooms(entrance, "east", hall);
        connectRooms(hall, "north", supply);
        connectRooms(hall, "east", trap);
        connectRooms(hall, "south", exit);
        connectRooms(trap, "east", treasure);
        connectRooms(treasure, "east", exit);

        entrance.addItem(new Item("old-map", "残旧地图", "标记着秘窟大致结构的羊皮纸。", ItemType.KEY, 1, 0, 0, 0));
        supply.addItem(new Item("clean-water", "清水", "饮用后可以恢复少量体力。", ItemType.SUPPLY, 2, 10, 0, 0));
        supply.addItem(new Item("dry-food", "干粮", "便于携带的补给，可以恢复体力。", ItemType.SUPPLY, 1, 6, 0, 0));
        trap.addItem(new Item("rope", "结实绳索", "帮助探险者携带更多物品的装备。", ItemType.EQUIPMENT, 3, 0, 5, 0));
        treasure.addItem(new Item("gold-crown", "金冠", "沉睡宝库中最耀眼的宝物。", ItemType.TREASURE, 6, 0, 0, 80));
        treasure.addItem(new Item("ruby", "红宝石", "像火焰一样闪烁的宝石。", ItemType.TREASURE, 4, 0, 0, 60));

        rooms.put(entrance.getId(), entrance);
        rooms.put(hall.getId(), hall);
        rooms.put(supply.getId(), supply);
        rooms.put(trap.getId(), trap);
        rooms.put(treasure.getId(), treasure);
        rooms.put(exit.getId(), exit);

        return rooms;
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

    private String createSessionId() {
        return "game-session-" + UUID.randomUUID();
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

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

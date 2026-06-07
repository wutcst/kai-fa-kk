/**
 * 该类是“芝麻开门”后端的游戏会话模型。
 * 游戏会话用于保存一局游戏运行过程中的玩家、地图、日志、暗语解锁情况和当前游戏状态。
 *
 * GameSession 类本身只负责保存和查询状态，不直接实现移动、返回、拾取、使用物品等业务规则。
 * 后续服务层会基于该会话对象组合出完整游戏流程。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示当前正在运行的一局游戏。
 */
public class GameSession {

    private final String id;
    private final String username;
    private final Player player;
    private final Map<String, Room> rooms;
    private final List<String> logs;
    private Long currentSaveId;
    private String previousRoomId;
    private String levelStartRoomId;
    private int levelStartMoney;
    private int levelStartMaxWeight;
    private List<String> levelStartInventoryItems;
    private int currentLevel;
    private int finalScore;
    private int highScore;
    private boolean passwordUnlocked;
    private GameStatus status;

    /**
     * 创建一局新的游戏会话。
     *
     * @param id 会话编号
     * @param player 当前玩家
     * @param rooms 游戏地图中的全部房间
     * @param status 初始游戏状态
     */
    public GameSession(String id, Player player, Map<String, Room> rooms, GameStatus status) {
        this(id, "", player, rooms, status);
    }

    /**
     * 创建一局新的游戏会话。
     *
     * @param id 会话编号
     * @param username 用户名
     * @param player 当前玩家
     * @param rooms 游戏地图中的全部房间
     * @param status 初始游戏状态
     */
    public GameSession(String id, String username, Player player, Map<String, Room> rooms, GameStatus status) {
        this.id = requireText(id, "会话编号不能为空");
        this.username = Objects.requireNonNullElse(username, "");
        this.player = Objects.requireNonNull(player, "玩家不能为空");
        this.rooms = new LinkedHashMap<>(Objects.requireNonNull(rooms, "房间地图不能为空"));
        this.status = Objects.requireNonNull(status, "游戏状态不能为空");
        this.logs = new ArrayList<>();
        this.currentSaveId = null;
        this.previousRoomId = null;
        this.levelStartRoomId = player.getCurrentRoomId();
        this.levelStartMoney = player.getMoney();
        this.levelStartMaxWeight = player.getMaxWeight();
        this.levelStartInventoryItems = new ArrayList<>();
        this.currentLevel = 1;
        this.finalScore = 0;
        this.highScore = 0;
        this.passwordUnlocked = false;
    }

    /**
     * 获取会话编号。
     *
     * @return 会话编号
     */
    public String getId() {
        return id;
    }

    /**
     * 获取当前玩家。
     *
     * @return 当前玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取当前会话所属用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取当前绑定的存档编号。
     *
     * @return 当前存档编号，新游戏尚未保存时为空
     */
    public Optional<Long> getCurrentSaveId() {
        return Optional.ofNullable(currentSaveId);
    }

    /**
     * 设置当前绑定的存档编号。
     *
     * @param currentSaveId 当前存档编号
     */
    public void setCurrentSaveId(Long currentSaveId) {
        this.currentSaveId = currentSaveId;
    }

    /**
     * 获取全部房间映射。
     *
     * @return 只读房间映射，键为房间编号，值为房间对象
     */
    public Map<String, Room> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    /**
     * 根据房间编号查找房间。
     *
     * @param roomId 房间编号
     * @return 找到的房间，如果不存在则为空
     */
    public Optional<Room> findRoom(String roomId) {
        return Optional.ofNullable(rooms.get(requireText(roomId, "房间编号不能为空")));
    }

    /**
     * 获取玩家当前所在房间。
     *
     * @return 玩家当前所在房间
     */
    public Room getCurrentRoom() {
        String roomId = player.getCurrentRoomId();
        return findRoom(roomId).orElseThrow(() -> new IllegalStateException("当前房间不存在：" + roomId));
    }

    /**
     * 获取玩家上一次所在房间编号。
     *
     * @return 上一次所在房间编号，如果尚未移动则为空
     */
    public Optional<String> getPreviousRoomId() {
        return Optional.ofNullable(previousRoomId);
    }

    /**
     * 清除上一房间记录。
     */
    public void clearPreviousRoomId() {
        this.previousRoomId = null;
    }

    /**
     * 记录玩家上一次所在房间编号。
     *
     * @param previousRoomId 上一次所在房间编号
     */
    public void setPreviousRoomId(String previousRoomId) {
        this.previousRoomId = requireText(previousRoomId, "上一个房间编号不能为空");
    }

    /**
     * 获取当前关卡编号。
     *
     * @return 当前关卡编号
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 设置当前关卡编号。
     *
     * @param currentLevel 当前关卡编号
     */
    public void setCurrentLevel(int currentLevel) {
        if (currentLevel < 1 || currentLevel > 3) {
            throw new IllegalArgumentException("当前关卡编号必须在 1 到 3 之间");
        }
        this.currentLevel = currentLevel;
    }

    /**
     * 记录当前关卡开始时的检查点。
     */
    public void markLevelCheckpoint() {
        this.levelStartRoomId = player.getCurrentRoomId();
        this.levelStartMoney = player.getMoney();
        this.levelStartMaxWeight = player.getMaxWeight();
        this.levelStartInventoryItems = player.getInventory().stream()
                .map(Item::getId)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /**
     * 获取当前关卡开始房间编号。
     *
     * @return 当前关卡开始房间编号
     */
    public String getLevelStartRoomId() {
        return levelStartRoomId;
    }

    /**
     * 设置当前关卡检查点数据，主要用于读取存档。
     *
     * @param levelStartRoomId 关卡开始房间编号
     * @param levelStartMoney 关卡开始金币
     * @param levelStartMaxWeight 关卡开始最大负重
     * @param levelStartInventoryItems 关卡开始背包物品编号
     */
    public void setLevelCheckpoint(String levelStartRoomId, int levelStartMoney, int levelStartMaxWeight,
            List<String> levelStartInventoryItems) {
        this.levelStartRoomId = requireText(levelStartRoomId, "关卡开始房间编号不能为空");
        this.levelStartMoney = levelStartMoney;
        this.levelStartMaxWeight = levelStartMaxWeight;
        this.levelStartInventoryItems = new ArrayList<>(Objects.requireNonNull(levelStartInventoryItems,
                "关卡开始背包不能为空"));
    }

    /**
     * 获取当前关卡开始金币。
     *
     * @return 当前关卡开始金币
     */
    public int getLevelStartMoney() {
        return levelStartMoney;
    }

    /**
     * 获取当前关卡开始最大负重。
     *
     * @return 当前关卡开始最大负重
     */
    public int getLevelStartMaxWeight() {
        return levelStartMaxWeight;
    }

    /**
     * 获取当前关卡开始背包物品编号。
     *
     * @return 当前关卡开始背包物品编号
     */
    public List<String> getLevelStartInventoryItems() {
        return Collections.unmodifiableList(levelStartInventoryItems);
    }

    /**
     * 获取最终分数。
     *
     * @return 最终分数
     */
    public int getFinalScore() {
        return finalScore;
    }

    /**
     * 设置最终分数。
     *
     * @param finalScore 最终分数
     */
    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    /**
     * 获取玩家历史最高分。
     *
     * @return 历史最高分
     */
    public int getHighScore() {
        return highScore;
    }

    /**
     * 设置玩家历史最高分。
     *
     * @param highScore 历史最高分
     */
    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    /**
     * 判断暗语是否已经解锁。
     *
     * @return 已解锁时返回 true，否则返回 false
     */
    public boolean isPasswordUnlocked() {
        return passwordUnlocked;
    }

    /**
     * 设置暗语解锁状态。
     *
     * @param passwordUnlocked 暗语是否已解锁
     */
    public void setPasswordUnlocked(boolean passwordUnlocked) {
        this.passwordUnlocked = passwordUnlocked;
    }

    /**
     * 获取游戏日志。
     *
     * @return 只读游戏日志列表
     */
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    /**
     * 添加一条游戏日志。
     *
     * @param log 日志内容
     */
    public void addLog(String log) {
        logs.add(requireText(log, "日志内容不能为空"));
    }

    /**
     * 获取当前游戏状态。
     *
     * @return 当前游戏状态
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * 修改当前游戏状态。
     *
     * @param status 新的游戏状态
     */
    public void setStatus(GameStatus status) {
        this.status = Objects.requireNonNull(status, "游戏状态不能为空");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

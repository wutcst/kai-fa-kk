/**
 * 该类是“芝麻开门”后端用于 SQLite 存档的数据记录。
 * 存档记录保存玩家、房间、背包、日志和暗语状态的可持久化字段。
 *
 * GameSaveRecord 不直接参与游戏规则判断，只作为 GameService 与 SQLite 存储之间的数据载体。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.persistence;

import cn.edu.whut.sept.sesame.model.GameStatus;

/**
 * 表示一条游戏存档记录。
 */
public class GameSaveRecord {

    private final String username;
    private final Long saveId;
    private final String saveName;
    private final String sessionId;
    private final String currentRoomId;
    private final String previousRoomId;
    private final int money;
    private final int stamina;
    private final int maxStamina;
    private final int maxWeight;
    private final int currentLevel;
    private final String levelStartRoomId;
    private final int levelStartMoney;
    private final int levelStartMaxWeight;
    private final String levelStartInventoryItems;
    private final int finalScore;
    private final GameStatus status;
    private final boolean passwordUnlocked;
    private final String inventoryItems;
    private final String roomItems;
    private final String logs;

    /**
     * 创建一条游戏存档记录。
     *
     * @param username 用户名
     * @param saveId 存档编号，新建存档时可以为空
     * @param saveName 存档名称
     * @param sessionId 会话编号
     * @param currentRoomId 当前房间编号
     * @param previousRoomId 上一个房间编号
     * @param money 当前金额
     * @param stamina 当前体力
     * @param maxStamina 最大体力
     * @param maxWeight 最大负重
     * @param currentLevel 当前关卡
     * @param levelStartRoomId 关卡开始房间编号
     * @param levelStartMoney 关卡开始金币
     * @param levelStartMaxWeight 关卡开始最大负重
     * @param levelStartInventoryItems 关卡开始背包物品
     * @param finalScore 最终分数
     * @param status 游戏状态
     * @param passwordUnlocked 是否已输入正确暗语
     * @param inventoryItems 背包物品编号列表
     * @param roomItems 房间物品分布
     * @param logs 游戏日志
     */
    public GameSaveRecord(String username, Long saveId, String saveName, String sessionId, String currentRoomId,
            String previousRoomId, int money, int stamina, int maxStamina, int maxWeight, int currentLevel,
            String levelStartRoomId, int levelStartMoney, int levelStartMaxWeight, String levelStartInventoryItems,
            int finalScore, GameStatus status, boolean passwordUnlocked, String inventoryItems, String roomItems,
            String logs) {
        this.username = username;
        this.saveId = saveId;
        this.saveName = saveName;
        this.sessionId = sessionId;
        this.currentRoomId = currentRoomId;
        this.previousRoomId = previousRoomId;
        this.money = money;
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.maxWeight = maxWeight;
        this.currentLevel = currentLevel;
        this.levelStartRoomId = levelStartRoomId;
        this.levelStartMoney = levelStartMoney;
        this.levelStartMaxWeight = levelStartMaxWeight;
        this.levelStartInventoryItems = levelStartInventoryItems;
        this.finalScore = finalScore;
        this.status = status;
        this.passwordUnlocked = passwordUnlocked;
        this.inventoryItems = inventoryItems;
        this.roomItems = roomItems;
        this.logs = logs;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取存档编号。
     *
     * @return 存档编号
     */
    public Long getSaveId() {
        return saveId;
    }

    /**
     * 获取存档名称。
     *
     * @return 存档名称
     */
    public String getSaveName() {
        return saveName;
    }

    /**
     * 获取会话编号。
     *
     * @return 会话编号
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 获取当前房间编号。
     *
     * @return 当前房间编号
     */
    public String getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * 获取上一个房间编号。
     *
     * @return 上一个房间编号
     */
    public String getPreviousRoomId() {
        return previousRoomId;
    }

    /**
     * 获取当前金额。
     *
     * @return 当前金额
     */
    public int getMoney() {
        return money;
    }

    /**
     * 获取当前体力。
     *
     * @return 当前体力
     */
    public int getStamina() {
        return stamina;
    }

    /**
     * 获取最大体力。
     *
     * @return 最大体力
     */
    public int getMaxStamina() {
        return maxStamina;
    }

    /**
     * 获取最大负重。
     *
     * @return 最大负重
     */
    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * 获取当前关卡。
     *
     * @return 当前关卡
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 获取关卡开始房间编号。
     *
     * @return 关卡开始房间编号
     */
    public String getLevelStartRoomId() {
        return levelStartRoomId;
    }

    /**
     * 获取关卡开始金币。
     *
     * @return 关卡开始金币
     */
    public int getLevelStartMoney() {
        return levelStartMoney;
    }

    /**
     * 获取关卡开始最大负重。
     *
     * @return 关卡开始最大负重
     */
    public int getLevelStartMaxWeight() {
        return levelStartMaxWeight;
    }

    /**
     * 获取关卡开始背包物品编号列表。
     *
     * @return 关卡开始背包物品编号列表
     */
    public String getLevelStartInventoryItems() {
        return levelStartInventoryItems;
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
     * 获取游戏状态。
     *
     * @return 游戏状态
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * 判断暗语是否已解锁。
     *
     * @return 已解锁时返回 true，否则返回 false
     */
    public boolean isPasswordUnlocked() {
        return passwordUnlocked;
    }

    /**
     * 获取背包物品编号列表。
     *
     * @return 背包物品编号列表
     */
    public String getInventoryItems() {
        return inventoryItems;
    }

    /**
     * 获取房间物品分布。
     *
     * @return 房间物品分布
     */
    public String getRoomItems() {
        return roomItems;
    }

    /**
     * 获取游戏日志。
     *
     * @return 游戏日志
     */
    public String getLogs() {
        return logs;
    }
}

/**
 * 该类是“芝麻开门”后端返回给前端的存档摘要对象。
 * 存档摘要用于展示一个玩家名下有哪些存档，方便玩家选择读取哪一个。
 *
 * GameSaveSummary 只包含列表展示需要的基本信息，不包含完整背包、房间物品和日志。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.dto;

/**
 * 表示一条游戏存档摘要。
 */
public class GameSaveSummary {

    private final long saveId;
    private final String saveName;
    private final String currentRoomId;
    private final int currentLevel;
    private final int money;
    private final int stamina;
    private final String status;
    private final String updatedAt;

    /**
     * 创建一条存档摘要。
     *
     * @param saveId 存档编号
     * @param saveName 存档名称
     * @param currentRoomId 当前房间编号
     * @param currentLevel 当前关卡
     * @param money 当前金币
     * @param stamina 当前体力
     * @param status 游戏状态
     * @param updatedAt 更新时间
     */
    public GameSaveSummary(long saveId, String saveName, String currentRoomId, int currentLevel,
            int money, int stamina, String status, String updatedAt) {
        this.saveId = saveId;
        this.saveName = saveName;
        this.currentRoomId = currentRoomId;
        this.currentLevel = currentLevel;
        this.money = money;
        this.stamina = stamina;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    /**
     * 获取存档编号。
     *
     * @return 存档编号
     */
    public long getSaveId() {
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
     * 获取当前房间编号。
     *
     * @return 当前房间编号
     */
    public String getCurrentRoomId() {
        return currentRoomId;
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
     * 获取当前金币。
     *
     * @return 当前金币
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
     * 获取游戏状态。
     *
     * @return 游戏状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public String getUpdatedAt() {
        return updatedAt;
    }
}

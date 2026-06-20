/**
 * 该类是“芝麻开门”后端返回给前端的玩家状态对象。
 * 玩家状态对象用于展示玩家金额、体力、负重、当前位置和背包内容。
 *
 * PlayerState 通常由 Player 模型转换得到，是前端状态面板的数据来源。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.dto;

import cn.edu.whut.sept.sesame.model.Player;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示前端可见的玩家状态。
 */
public class PlayerState {

    private final int money;
    private final int stamina;
    private final int maxStamina;
    private final int currentWeight;
    private final int maxWeight;
    private final String currentRoomId;
    private final List<ItemState> inventory;

    /**
     * 创建一个玩家状态对象。
     *
     * @param money 当前金额
     * @param stamina 当前体力
     * @param maxStamina 最大体力
     * @param currentWeight 当前负重
     * @param maxWeight 最大负重
     * @param currentRoomId 当前房间编号
     * @param inventory 背包物品状态列表
     */
    public PlayerState(int money, int stamina, int maxStamina, int currentWeight, int maxWeight,
            String currentRoomId, List<ItemState> inventory) {
        this.money = money;
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.currentWeight = currentWeight;
        this.maxWeight = maxWeight;
        this.currentRoomId = currentRoomId;
        this.inventory = List.copyOf(inventory);
    }

    /**
     * 根据玩家模型创建玩家状态对象。
     *
     * @param player 玩家模型
     * @return 玩家状态对象
     */
    public static PlayerState from(Player player) {
        Objects.requireNonNull(player, "玩家不能为空");
        List<ItemState> inventory = player.getInventory().stream()
                .map(ItemState::from)
                .collect(Collectors.toList());
        return new PlayerState(player.getMoney(), player.getStamina(), player.getMaxStamina(),
                player.getCurrentWeight(), player.getMaxWeight(), player.getCurrentRoomId(), inventory);
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
     * 获取当前负重。
     *
     * @return 当前负重
     */
    public int getCurrentWeight() {
        return currentWeight;
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
     * 获取当前房间编号。
     *
     * @return 当前房间编号
     */
    public String getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * 获取背包物品状态列表。
     *
     * @return 背包物品状态列表
     */
    public List<ItemState> getInventory() {
        return inventory;
    }
}

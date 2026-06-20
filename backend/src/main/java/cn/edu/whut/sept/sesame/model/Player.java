/**
 * 该类是“芝麻开门”后端的玩家模型。
 * 玩家模型用于记录游戏过程中的金额、体力、负重、背包和当前位置。
 *
 * Player 类提供支付金额、扣除体力、恢复体力、拾取物品、丢弃物品和移动位置等基础行为。
 * 后续服务层会基于这些行为组合出移动、救援、补给和通关等完整游戏流程。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示当前游戏玩家。
 */
public class Player {

    /**
     * 玩家初始金额。
     */
    public static final int DEFAULT_MONEY = 100;

    /**
     * 玩家初始体力和最大体力。
     */
    public static final int DEFAULT_STAMINA = 30;

    /**
     * 玩家初始最大负重。
     */
    public static final int DEFAULT_MAX_WEIGHT = 20;

    private int money;
    private int stamina;
    private int maxStamina;
    private int maxWeight;
    private String currentRoomId;
    private final List<Item> inventory;

    /**
     * 创建一个使用默认状态的玩家。
     */
    public Player() {
        this(null);
    }

    /**
     * 创建一个使用默认状态并位于指定房间的玩家。
     *
     * @param currentRoomId 当前房间编号
     */
    public Player(String currentRoomId) {
        this(currentRoomId, DEFAULT_MONEY, DEFAULT_STAMINA, DEFAULT_STAMINA, DEFAULT_MAX_WEIGHT);
    }

    /**
     * 创建一个使用指定初始数值的玩家。
     *
     * @param currentRoomId 当前房间编号
     * @param money 初始金额
     * @param stamina 初始体力
     * @param maxStamina 最大体力
     * @param maxWeight 最大负重
     */
    public Player(String currentRoomId, int money, int stamina, int maxStamina, int maxWeight) {
        if (money < 0) {
            throw new IllegalArgumentException("初始金额不能为负数");
        }
        if (stamina < 0 || maxStamina < 0 || stamina > maxStamina) {
            throw new IllegalArgumentException("初始体力必须在 0 到最大体力之间");
        }
        if (maxWeight < 0) {
            throw new IllegalArgumentException("最大负重不能为负数");
        }
        this.money = money;
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.maxWeight = maxWeight;
        this.currentRoomId = currentRoomId;
        this.inventory = new ArrayList<>();
    }

    /**
     * 获取玩家当前金额。
     *
     * @return 当前金额
     */
    public int getMoney() {
        return money;
    }

    /**
     * 获取玩家当前体力。
     *
     * @return 当前体力
     */
    public int getStamina() {
        return stamina;
    }

    /**
     * 获取玩家最大体力。
     *
     * @return 最大体力
     */
    public int getMaxStamina() {
        return maxStamina;
    }

    /**
     * 获取玩家当前负重。
     *
     * @return 当前负重
     */
    public int getCurrentWeight() {
        return inventory.stream().mapToInt(Item::getWeight).sum();
    }

    /**
     * 获取玩家最大负重。
     *
     * @return 最大负重
     */
    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * 获取玩家背包物品列表。
     *
     * @return 只读背包物品列表
     */
    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    /**
     * 获取玩家当前所在房间编号。
     *
     * @return 当前房间编号
     */
    public String getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * 将玩家移动到指定房间。
     *
     * @param roomId 目标房间编号
     */
    public void moveTo(String roomId) {
        this.currentRoomId = requireText(roomId, "房间编号不能为空");
    }

    /**
     * 支付指定金额。
     *
     * @param amount 需要支付的金额
     * @return 金额足够并支付成功时返回 true，否则返回 false
     */
    public boolean pay(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("支付金额不能为负数");
        }
        if (money < amount) {
            return false;
        }
        money -= amount;
        return true;
    }

    /**
     * 增加指定金额。
     *
     * @param amount 需要增加的金额
     */
    public void earn(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("增加金额不能为负数");
        }
        money += amount;
    }

    /**
     * 扣除指定体力，体力最低降为 0。
     *
     * @param amount 需要扣除的体力
     */
    public void decreaseStamina(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("扣除体力不能为负数");
        }
        stamina = Math.max(0, stamina - amount);
    }

    /**
     * 恢复指定体力，恢复后不能超过最大体力。
     *
     * @param amount 需要恢复的体力
     */
    public void restoreStamina(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("恢复体力不能为负数");
        }
        stamina = Math.min(maxStamina, stamina + amount);
    }

    /**
     * 增加玩家最大负重。
     *
     * @param amount 需要增加的最大负重
     */
    public void increaseMaxWeight(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("增加负重不能为负数");
        }
        maxWeight += amount;
    }

    /**
     * 判断玩家是否还能携带指定物品。
     *
     * @param item 待判断的物品
     * @return 不超过最大负重时返回 true，否则返回 false
     */
    public boolean canCarry(Item item) {
        Objects.requireNonNull(item, "物品不能为空");
        return getCurrentWeight() + item.getWeight() <= maxWeight;
    }

    /**
     * 将物品加入玩家背包。
     *
     * @param item 待拾取物品
     * @return 物品加入成功时返回 true，超过负重上限时返回 false
     */
    public boolean addItem(Item item) {
        if (!canCarry(item)) {
            return false;
        }
        inventory.add(item);
        return true;
    }

    /**
     * 根据物品编号从背包中移除物品。
     *
     * @param itemId 物品编号
     * @return 被移除的物品，如果不存在则为空
     */
    public Optional<Item> removeItem(String itemId) {
        requireText(itemId, "物品编号不能为空");
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (item.getId().equals(itemId)) {
                inventory.remove(i);
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * 判断背包中是否存在指定物品。
     *
     * @param itemId 物品编号
     * @return 背包存在该物品时返回 true，否则返回 false
     */
    public boolean hasItem(String itemId) {
        requireText(itemId, "物品编号不能为空");
        return inventory.stream().anyMatch(item -> item.getId().equals(itemId));
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

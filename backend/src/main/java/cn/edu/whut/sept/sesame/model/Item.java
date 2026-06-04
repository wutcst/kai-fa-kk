/**
 * 该类是“芝麻开门”后端的物品模型。
 * 物品用于描述玩家可以在房间中发现、拾取、丢弃或使用的对象。
 *
 * Item 类包含物品编号、名称、描述、类型、重量、体力效果、负重效果和金额价值。
 * 后续业务服务可以根据这些字段实现补给、装备、宝物和钥匙等玩法规则。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.model;

import java.util.Objects;

/**
 * 表示游戏中的一个物品。
 */
public class Item {

    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final int weight;
    private final int staminaEffect;
    private final int maxWeightEffect;
    private final int moneyValue;

    /**
     * 创建一个物品。
     *
     * @param id 物品唯一编号
     * @param name 物品名称
     * @param description 物品描述
     * @param type 物品类型
     * @param weight 物品重量
     * @param staminaEffect 使用后产生的体力变化值
     * @param maxWeightEffect 使用后产生的最大负重变化值
     * @param moneyValue 物品对应的金额价值
     */
    public Item(String id, String name, String description, ItemType type, int weight,
            int staminaEffect, int maxWeightEffect, int moneyValue) {
        this.id = requireText(id, "物品编号不能为空");
        this.name = requireText(name, "物品名称不能为空");
        this.description = Objects.requireNonNullElse(description, "");
        this.type = Objects.requireNonNull(type, "物品类型不能为空");
        if (weight < 0) {
            throw new IllegalArgumentException("物品重量不能为负数");
        }
        this.weight = weight;
        this.staminaEffect = staminaEffect;
        this.maxWeightEffect = maxWeightEffect;
        this.moneyValue = moneyValue;
    }

    /**
     * 获取物品唯一编号。
     *
     * @return 物品唯一编号
     */
    public String getId() {
        return id;
    }

    /**
     * 获取物品名称。
     *
     * @return 物品名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取物品描述。
     *
     * @return 物品描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取物品类型。
     *
     * @return 物品类型
     */
    public ItemType getType() {
        return type;
    }

    /**
     * 获取物品重量。
     *
     * @return 物品重量
     */
    public int getWeight() {
        return weight;
    }

    /**
     * 获取物品使用后的体力效果。
     *
     * @return 体力变化值
     */
    public int getStaminaEffect() {
        return staminaEffect;
    }

    /**
     * 获取物品使用后的最大负重效果。
     *
     * @return 最大负重变化值
     */
    public int getMaxWeightEffect() {
        return maxWeightEffect;
    }

    /**
     * 获取物品金额价值。
     *
     * @return 金额价值
     */
    public int getMoneyValue() {
        return moneyValue;
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

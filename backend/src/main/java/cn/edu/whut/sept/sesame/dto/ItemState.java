/**
 * 该类是“芝麻开门”后端返回给前端的物品状态对象。
 * 物品状态对象只包含展示和交互需要的数据，避免前端直接依赖后端内部模型。
 *
 * ItemState 通常由 Item 模型转换得到，用于展示房间物品和玩家背包物品。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.dto;

import cn.edu.whut.sept.sesame.model.Item;
import cn.edu.whut.sept.sesame.model.ItemType;
import java.util.Objects;

/**
 * 表示前端可见的一个物品状态。
 */
public class ItemState {

    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final int weight;
    private final int staminaEffect;
    private final int maxWeightEffect;
    private final int moneyValue;

    /**
     * 创建一个物品状态对象。
     *
     * @param id 物品编号
     * @param name 物品名称
     * @param description 物品描述
     * @param type 物品类型
     * @param weight 物品重量
     * @param staminaEffect 体力效果
     * @param maxWeightEffect 最大负重效果
     * @param moneyValue 金额价值
     */
    public ItemState(String id, String name, String description, ItemType type, int weight,
            int staminaEffect, int maxWeightEffect, int moneyValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.weight = weight;
        this.staminaEffect = staminaEffect;
        this.maxWeightEffect = maxWeightEffect;
        this.moneyValue = moneyValue;
    }

    /**
     * 根据物品模型创建物品状态对象。
     *
     * @param item 物品模型
     * @return 物品状态对象
     */
    public static ItemState from(Item item) {
        Objects.requireNonNull(item, "物品不能为空");
        return new ItemState(item.getId(), item.getName(), item.getDescription(), item.getType(),
                item.getWeight(), item.getStaminaEffect(), item.getMaxWeightEffect(), item.getMoneyValue());
    }

    /**
     * 获取物品编号。
     *
     * @return 物品编号
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
     * 获取物品体力效果。
     *
     * @return 体力效果
     */
    public int getStaminaEffect() {
        return staminaEffect;
    }

    /**
     * 获取物品最大负重效果。
     *
     * @return 最大负重效果
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
}

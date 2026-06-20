/**
 * 商店商品目录数据传输对象。
 */
package cn.edu.whut.sept.sesame.dto;

import cn.edu.whut.sept.sesame.model.Item;
import cn.edu.whut.sept.sesame.model.ItemType;
import java.util.Objects;

/**
 * 表示一个可在商店购买的商品及其购买价格。
 */
public class ShopCatalogItem {

    private final String itemId;
    private final String name;
    private final String description;
    private final ItemType type;
    private final int price;
    private final int weight;
    private final int staminaEffect;
    private final int maxWeightEffect;
    private final int moneyValue;

    /**
     * 创建商店商品目录项。
     *
     * @param itemId 物品编号
     * @param name 物品名称
     * @param description 物品描述
     * @param type 物品类型
     * @param price 购买价格
     * @param weight 物品重量
     * @param staminaEffect 体力效果
     * @param maxWeightEffect 最大负重效果
     * @param moneyValue 出售或结算价值
     */
    public ShopCatalogItem(String itemId, String name, String description, ItemType type, int price,
            int weight, int staminaEffect, int maxWeightEffect, int moneyValue) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.weight = weight;
        this.staminaEffect = staminaEffect;
        this.maxWeightEffect = maxWeightEffect;
        this.moneyValue = moneyValue;
    }

    /**
     * 根据物品模型和购买价格创建目录项。
     *
     * @param item 物品模型
     * @param price 购买价格
     * @return 商店商品目录项
     */
    public static ShopCatalogItem from(Item item, int price) {
        Objects.requireNonNull(item, "物品不能为空");
        return new ShopCatalogItem(item.getId(), item.getName(), item.getDescription(), item.getType(), price,
                item.getWeight(), item.getStaminaEffect(), item.getMaxWeightEffect(), item.getMoneyValue());
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public int getWeight() {
        return weight;
    }

    public int getStaminaEffect() {
        return staminaEffect;
    }

    public int getMaxWeightEffect() {
        return maxWeightEffect;
    }

    public int getMoneyValue() {
        return moneyValue;
    }
}

/**
 * 该类是“芝麻开门”后端物品模型的测试类。
 * 测试内容包括物品字段保存、物品类型区分和非法重量校验。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

/**
 * 测试 Item 和 ItemType 模型。
 */
class ItemTest {

    /**
     * 确认物品可以保存所有基础属性。
     */
    @Test
    void itemStoresBasicProperties() {
        Item item = new Item("water", "清水", "恢复体力的补给", ItemType.SUPPLY, 2, 10, 0, 0);

        assertEquals("water", item.getId());
        assertEquals("清水", item.getName());
        assertEquals("恢复体力的补给", item.getDescription());
        assertEquals(ItemType.SUPPLY, item.getType());
        assertEquals(2, item.getWeight());
        assertEquals(10, item.getStaminaEffect());
        assertEquals(0, item.getMaxWeightEffect());
        assertEquals(0, item.getMoneyValue());
    }

    /**
     * 确认物品类型包含基础玩法需要的四种类型。
     */
    @Test
    void itemTypesContainRequiredValues() {
        EnumSet<ItemType> types = EnumSet.allOf(ItemType.class);

        assertTrue(types.contains(ItemType.TREASURE));
        assertTrue(types.contains(ItemType.SUPPLY));
        assertTrue(types.contains(ItemType.EQUIPMENT));
        assertTrue(types.contains(ItemType.KEY));
    }

    /**
     * 确认物品重量不能为负数。
     */
    @Test
    void itemRejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class,
                () -> new Item("bad", "异常物品", "", ItemType.TREASURE, -1, 0, 0, 0));
    }
}

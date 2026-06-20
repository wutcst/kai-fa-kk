/**
 * 该类是“芝麻开门”后端房间模型的测试类。
 * 测试内容包括房间出口、房间物品和特殊规则标记。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 测试 Room 模型。
 */
class RoomTest {

    /**
     * 确认房间可以配置出口并根据方向找到目标房间。
     */
    @Test
    void roomCanConfigureExits() {
        Room room = new Room("hall", "石门大厅", "刻有古老文字的大厅");

        room.addExit("east", "supply");
        room.addExit("WEST", "trap");

        assertEquals("supply", room.getExit("east").orElseThrow());
        assertEquals("trap", room.getExit("west").orElseThrow());
        assertTrue(room.getExit("north").isEmpty());
        assertEquals(2, room.getExits().size());
    }

    /**
     * 确认房间可以添加、查找和移除物品。
     */
    @Test
    void roomCanManageItems() {
        Room room = new Room("supply", "补给洞室", "摆放补给的洞室");
        Item water = new Item("water", "清水", "恢复体力", ItemType.SUPPLY, 2, 10, 0, 0);

        room.addItem(water);

        assertEquals(1, room.getItems().size());
        assertTrue(room.findItem("water").isPresent());
        assertTrue(room.removeItem("water").isPresent());
        assertFalse(room.findItem("water").isPresent());
    }

    /**
     * 确认房间可以标记出口和暗语规则。
     */
    @Test
    void roomCanStoreSpecialRuleFlags() {
        Room room = new Room("exit", "出口", "秘窟出口", true, true);

        assertTrue(room.isExit());
        assertTrue(room.isRequiresPassword());

        room.setExit(false);
        room.setRequiresPassword(false);

        assertFalse(room.isExit());
        assertFalse(room.isRequiresPassword());
    }
}

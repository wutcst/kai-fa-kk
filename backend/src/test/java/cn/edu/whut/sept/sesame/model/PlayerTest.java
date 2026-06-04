/**
 * 该类是“芝麻开门”后端玩家模型的测试类。
 * 测试内容包括玩家初始状态、金额、体力、负重、背包和当前位置。
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
 * 测试 Player 模型。
 */
class PlayerTest {

    /**
     * 确认玩家初始状态符合游戏规则。
     */
    @Test
    void playerHasExpectedInitialState() {
        Player player = new Player("entrance");

        assertEquals(100, player.getMoney());
        assertEquals(30, player.getStamina());
        assertEquals(30, player.getMaxStamina());
        assertEquals(0, player.getCurrentWeight());
        assertEquals(20, player.getMaxWeight());
        assertEquals("entrance", player.getCurrentRoomId());
        assertTrue(player.getInventory().isEmpty());
    }

    /**
     * 确认玩家可以支付金额且金额不足时不会扣款。
     */
    @Test
    void playerCanPayWhenMoneyIsEnough() {
        Player player = new Player();

        assertTrue(player.pay(10));
        assertEquals(90, player.getMoney());
        assertFalse(player.pay(200));
        assertEquals(90, player.getMoney());
    }

    /**
     * 确认玩家体力可以扣除和恢复，且恢复不超过最大体力。
     */
    @Test
    void playerCanDecreaseAndRestoreStamina() {
        Player player = new Player();

        player.decreaseStamina(12);
        assertEquals(18, player.getStamina());

        player.restoreStamina(20);
        assertEquals(30, player.getStamina());
    }

    /**
     * 确认玩家拾取物品时会检查负重上限。
     */
    @Test
    void playerCannotCarryItemOverWeightLimit() {
        Player player = new Player();
        Item heavyItem = new Item("stone", "巨石", "过重的石头", ItemType.TREASURE, 21, 0, 0, 0);

        assertFalse(player.addItem(heavyItem));
        assertEquals(0, player.getCurrentWeight());
        assertTrue(player.getInventory().isEmpty());
    }

    /**
     * 确认玩家可以拾取和丢弃物品。
     */
    @Test
    void playerCanAddAndRemoveInventoryItem() {
        Player player = new Player();
        Item key = new Item("key", "铜钥匙", "打开石门的钥匙", ItemType.KEY, 1, 0, 0, 0);

        assertTrue(player.addItem(key));
        assertTrue(player.hasItem("key"));
        assertEquals(1, player.getCurrentWeight());

        assertTrue(player.removeItem("key").isPresent());
        assertFalse(player.hasItem("key"));
        assertEquals(0, player.getCurrentWeight());
    }
}

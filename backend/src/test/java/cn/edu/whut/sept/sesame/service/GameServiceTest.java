/**
 * 该类是“芝麻开门”后端游戏会话服务的测试类。
 * 测试内容包括开始游戏、初始化地图、读取玩家状态、当前房间状态和游戏日志。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.edu.whut.sept.sesame.dto.GameState;
import cn.edu.whut.sept.sesame.model.GameSession;
import cn.edu.whut.sept.sesame.model.GameStatus;
import cn.edu.whut.sept.sesame.model.Player;
import org.junit.jupiter.api.Test;

/**
 * 测试 GameService 服务。
 */
class GameServiceTest {

    /**
     * 确认开始游戏后会创建玩家并进入初始房间。
     */
    @Test
    void startGameCreatesPlayerAtEntrance() {
        GameService service = new GameService();

        GameState state = service.startGame();

        assertFalse(state.getSessionId().isBlank());
        assertEquals(GameStatus.IN_PROGRESS, state.getStatus());
        assertEquals(GameService.START_ROOM_ID, state.getPlayer().getCurrentRoomId());
        assertEquals("秘窟入口", state.getCurrentRoom().getName());
        assertEquals(Player.DEFAULT_MONEY - GameService.TICKET_COST, state.getPlayer().getMoney());
        assertEquals(Player.DEFAULT_STAMINA, state.getPlayer().getStamina());
        assertFalse(state.getLogs().isEmpty());
    }

    /**
     * 确认金额不足时无法开始探索。
     */
    @Test
    void startGameFailsWhenMoneyIsNotEnoughForTicket() {
        GameService service = new GameService();

        GameState state = service.startGame("poor-player", GameService.TICKET_COST - 1);

        assertEquals(GameStatus.FAILED, state.getStatus());
        assertEquals(GameService.TICKET_COST - 1, state.getPlayer().getMoney());
        assertEquals("金额不足，无法开始探索。", state.getMessage());
    }

    /**
     * 确认开始游戏后会生成完整地图、出口关系和初始物品。
     */
    @Test
    void startGameCreatesMapRoomsExitsAndItems() {
        GameService service = new GameService();

        GameState state = service.startGame();
        GameSession session = service.getCurrentSession(state.getSessionId());

        assertEquals(6, session.getRooms().size());
        assertTrue(session.findRoom("stone-hall").orElseThrow().getExit("north").isPresent());
        assertTrue(session.findRoom("supply-room").orElseThrow().getItems().size() >= 2);
        assertTrue(session.findRoom("treasure-room").orElseThrow().isRequiresPassword());
        assertTrue(session.findRoom("final-exit").orElseThrow().isExit());
        assertEquals("stone-hall", session.findRoom("final-exit").orElseThrow().getExit("north").orElseThrow());
        assertEquals("treasure-room", session.findRoom("final-exit").orElseThrow().getExit("west").orElseThrow());
    }

    /**
     * 确认 getState 可以返回当前房间、玩家、物品和日志。
     */
    @Test
    void getStateReturnsCurrentPlayerRoomItemsAndLogs() {
        GameService service = new GameService();

        GameState startedState = service.startGame();
        service.getCurrentSession(startedState.getSessionId()).getPlayer().moveTo("supply-room");
        GameState state = service.getState(startedState.getSessionId());

        assertEquals("supply-room", state.getCurrentRoom().getId());
        assertEquals("补给洞室", state.getCurrentRoom().getName());
        assertFalse(state.getCurrentRoom().getItems().isEmpty());
        assertEquals("supply-room", state.getPlayer().getCurrentRoomId());
        assertFalse(state.getLogs().isEmpty());
    }

    /**
     * 确认合法移动会改变位置并扣除体力。
     */
    @Test
    void moveChangesRoomAndConsumesStamina() {
        GameService service = new GameService();
        GameState startedState = service.startGame();

        GameState movedState = service.move(startedState.getSessionId(), "east");

        assertEquals("stone-hall", movedState.getPlayer().getCurrentRoomId());
        assertEquals("石门大厅", movedState.getCurrentRoom().getName());
        assertEquals(Player.DEFAULT_STAMINA - GameService.MOVE_STAMINA_COST, movedState.getPlayer().getStamina());
    }

    /**
     * 确认非法方向不会改变位置和体力。
     */
    @Test
    void moveWithInvalidDirectionDoesNotChangePlayerState() {
        GameService service = new GameService();
        GameState startedState = service.startGame();

        GameState movedState = service.move(startedState.getSessionId(), "north");

        assertEquals(GameService.START_ROOM_ID, movedState.getPlayer().getCurrentRoomId());
        assertEquals(Player.DEFAULT_STAMINA, movedState.getPlayer().getStamina());
        assertEquals("当前房间没有通向 north 的出口。", movedState.getMessage());
    }

    /**
     * 确认体力不足时不能移动。
     */
    @Test
    void moveFailsWhenStaminaIsNotEnough() {
        GameService service = new GameService();
        GameState startedState = service.startGame();
        service.getCurrentSession(startedState.getSessionId()).getPlayer()
                .decreaseStamina(Player.DEFAULT_STAMINA - 1);

        GameState movedState = service.move(startedState.getSessionId(), "east");

        assertEquals(GameService.START_ROOM_ID, movedState.getPlayer().getCurrentRoomId());
        assertEquals(1, movedState.getPlayer().getStamina());
        assertEquals("体力不足，无法移动。", movedState.getMessage());
    }

    /**
     * 确认返回指令可以让玩家回到上一个房间并扣除体力。
     */
    @Test
    void backReturnsPlayerToPreviousRoomAndConsumesStamina() {
        GameService service = new GameService();
        GameState startedState = service.startGame();
        GameState movedState = service.move(startedState.getSessionId(), "east");

        GameState backedState = service.back(movedState.getSessionId());

        assertEquals(GameService.START_ROOM_ID, backedState.getPlayer().getCurrentRoomId());
        assertEquals(Player.DEFAULT_STAMINA - GameService.MOVE_STAMINA_COST * 2,
                backedState.getPlayer().getStamina());
        assertEquals("你返回了秘窟入口。", backedState.getMessage());
    }

    /**
     * 确认拾取物品会从房间移除物品并放入背包。
     */
    @Test
    void takeItemMovesItemFromRoomToInventory() {
        GameService service = new GameService();
        GameState startedState = service.startGame();

        GameState state = service.takeItem(startedState.getSessionId(), "old-map");

        assertTrue(state.getPlayer().getInventory().stream()
                .anyMatch(item -> item.getId().equals("old-map")));
        assertTrue(state.getCurrentRoom().getItems().stream()
                .noneMatch(item -> item.getId().equals("old-map")));
        assertEquals(1, state.getPlayer().getCurrentWeight());
    }

    /**
     * 确认背包负重不足时不能拾取物品。
     */
    @Test
    void takeItemFailsWhenWeightLimitIsExceeded() {
        GameService service = new GameService();
        GameState startedState = service.startGame("weak-player", Player.DEFAULT_MONEY);
        service.getCurrentSession(startedState.getSessionId()).getPlayer()
                .addItem(new cn.edu.whut.sept.sesame.model.Item("stone", "石块", "用于压满背包的测试物品。",
                        cn.edu.whut.sept.sesame.model.ItemType.KEY, Player.DEFAULT_MAX_WEIGHT, 0, 0, 0));

        GameState state = service.takeItem(startedState.getSessionId(), "old-map");

        assertEquals("背包负重不足，无法拾取残旧地图。", state.getMessage());
        assertTrue(state.getCurrentRoom().getItems().stream()
                .anyMatch(item -> item.getId().equals("old-map")));
    }

    /**
     * 确认丢弃物品会从背包移除物品并放回当前房间。
     */
    @Test
    void dropItemMovesItemFromInventoryToRoom() {
        GameService service = new GameService();
        GameState startedState = service.startGame();
        service.takeItem(startedState.getSessionId(), "old-map");

        GameState state = service.dropItem(startedState.getSessionId(), "old-map");

        assertTrue(state.getPlayer().getInventory().isEmpty());
        assertTrue(state.getCurrentRoom().getItems().stream()
                .anyMatch(item -> item.getId().equals("old-map")));
        assertEquals(0, state.getPlayer().getCurrentWeight());
    }

    /**
     * 确认使用补给物品会恢复体力并消耗该物品。
     */
    @Test
    void useSupplyRestoresStaminaAndRemovesItem() {
        GameService service = new GameService();
        GameState startedState = service.startGame();
        GameState supplyRoomState = service.move(startedState.getSessionId(), "east");
        supplyRoomState = service.move(supplyRoomState.getSessionId(), "north");
        service.takeItem(supplyRoomState.getSessionId(), "clean-water");
        service.getCurrentSession(supplyRoomState.getSessionId()).getPlayer().decreaseStamina(12);

        GameState state = service.useItem(supplyRoomState.getSessionId(), "clean-water");

        assertEquals(18, state.getPlayer().getStamina());
        assertTrue(state.getPlayer().getInventory().stream()
                .noneMatch(item -> item.getId().equals("clean-water")));
        assertEquals("你使用了清水，恢复体力 10。", state.getMessage());
    }

    /**
     * 确认使用装备物品会增加最大负重并消耗该物品。
     */
    @Test
    void useEquipmentIncreasesMaxWeightAndRemovesItem() {
        GameService service = new GameService();
        GameState startedState = service.startGame();
        GameState trapRoomState = service.move(startedState.getSessionId(), "east");
        trapRoomState = service.move(trapRoomState.getSessionId(), "east");
        service.takeItem(trapRoomState.getSessionId(), "rope");

        GameState state = service.useItem(trapRoomState.getSessionId(), "rope");

        assertEquals(Player.DEFAULT_MAX_WEIGHT + 5, state.getPlayer().getMaxWeight());
        assertTrue(state.getPlayer().getInventory().stream()
                .noneMatch(item -> item.getId().equals("rope")));
        assertEquals("你使用了结实绳索，最大负重增加 5。", state.getMessage());
    }

    /**
     * 确认游戏尚未开始时不能直接读取状态。
     */
    @Test
    void getStateBeforeStartThrowsException() {
        GameService service = new GameService();

        assertThrows(IllegalStateException.class, () -> service.getState("missing-session"));
    }
}

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
     * 确认游戏尚未开始时不能直接读取状态。
     */
    @Test
    void getStateBeforeStartThrowsException() {
        GameService service = new GameService();

        assertThrows(IllegalStateException.class, () -> service.getState("missing-session"));
    }
}

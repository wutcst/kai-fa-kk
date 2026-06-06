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
        assertEquals(Player.DEFAULT_MONEY, state.getPlayer().getMoney());
        assertEquals(Player.DEFAULT_STAMINA, state.getPlayer().getStamina());
        assertFalse(state.getLogs().isEmpty());
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
     * 确认游戏尚未开始时不能直接读取状态。
     */
    @Test
    void getStateBeforeStartThrowsException() {
        GameService service = new GameService();

        assertThrows(IllegalStateException.class, () -> service.getState("missing-session"));
    }
}

/**
 * 该类是“芝麻开门”后端返回给前端的完整游戏状态对象。
 * 完整游戏状态包含玩家状态、当前房间状态、游戏日志、提示消息和当前游戏阶段。
 *
 * GameState 由 GameService 生成，后续 REST 接口可以直接将该对象返回给前端页面。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.dto;

import cn.edu.whut.sept.sesame.model.GameSession;
import cn.edu.whut.sept.sesame.model.GameStatus;
import java.util.List;
import java.util.Objects;

/**
 * 表示前端一次刷新所需要的完整游戏状态。
 */
public class GameState {

    private final String sessionId;
    private final GameStatus status;
    private final PlayerState player;
    private final RoomState currentRoom;
    private final List<String> logs;
    private final String message;

    /**
     * 创建一个完整游戏状态对象。
     *
     * @param sessionId 会话编号
     * @param status 游戏状态
     * @param player 玩家状态
     * @param currentRoom 当前房间状态
     * @param logs 游戏日志
     * @param message 本次操作提示消息
     */
    public GameState(String sessionId, GameStatus status, PlayerState player, RoomState currentRoom,
            List<String> logs, String message) {
        this.sessionId = sessionId;
        this.status = status;
        this.player = player;
        this.currentRoom = currentRoom;
        this.logs = List.copyOf(logs);
        this.message = message;
    }

    /**
     * 根据游戏会话创建完整游戏状态对象。
     *
     * @param session 游戏会话
     * @param message 本次操作提示消息
     * @return 完整游戏状态对象
     */
    public static GameState from(GameSession session, String message) {
        Objects.requireNonNull(session, "游戏会话不能为空");
        return new GameState(session.getId(), session.getStatus(), PlayerState.from(session.getPlayer()),
                RoomState.from(session.getCurrentRoom()), session.getLogs(), message);
    }

    /**
     * 获取会话编号。
     *
     * @return 会话编号
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 获取游戏状态。
     *
     * @return 游戏状态
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * 获取玩家状态。
     *
     * @return 玩家状态
     */
    public PlayerState getPlayer() {
        return player;
    }

    /**
     * 获取当前房间状态。
     *
     * @return 当前房间状态
     */
    public RoomState getCurrentRoom() {
        return currentRoom;
    }

    /**
     * 获取游戏日志。
     *
     * @return 游戏日志
     */
    public List<String> getLogs() {
        return logs;
    }

    /**
     * 获取本次操作提示消息。
     *
     * @return 本次操作提示消息
     */
    public String getMessage() {
        return message;
    }
}

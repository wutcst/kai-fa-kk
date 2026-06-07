/**
 * 该类是“芝麻开门”后端返回给前端的完整游戏状态对象。
 * 完整游戏状态包含玩家状态、当前房间状态、游戏日志、提示消息、关卡、暗语状态和当前游戏阶段。
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
    private final boolean passwordUnlocked;
    private final int currentLevel;
    private final int finalScore;
    private final int highScore;

    /**
     * 创建一个完整游戏状态对象。
     *
     * @param sessionId 会话编号
     * @param status 游戏状态
     * @param player 玩家状态
     * @param currentRoom 当前房间状态
     * @param logs 游戏日志
     * @param message 本次操作提示消息
     * @param passwordUnlocked 暗语是否已解锁
     * @param currentLevel 当前关卡
     * @param finalScore 最终分数
     * @param highScore 历史最高分
     */
    public GameState(String sessionId, GameStatus status, PlayerState player, RoomState currentRoom,
            List<String> logs, String message, boolean passwordUnlocked, int currentLevel, int finalScore,
            int highScore) {
        this.sessionId = sessionId;
        this.status = status;
        this.player = player;
        this.currentRoom = currentRoom;
        this.logs = List.copyOf(logs);
        this.message = message;
        this.passwordUnlocked = passwordUnlocked;
        this.currentLevel = currentLevel;
        this.finalScore = finalScore;
        this.highScore = highScore;
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
                RoomState.from(session.getCurrentRoom()), session.getLogs(), message, session.isPasswordUnlocked(),
                session.getCurrentLevel(), session.getFinalScore(), session.getHighScore());
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

    /**
     * 判断暗语是否已经解锁。
     *
     * @return 已解锁时返回 true，否则返回 false
     */
    public boolean isPasswordUnlocked() {
        return passwordUnlocked;
    }

    /**
     * 获取当前关卡。
     *
     * @return 当前关卡
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * 获取最终分数。
     *
     * @return 最终分数
     */
    public int getFinalScore() {
        return finalScore;
    }

    /**
     * 获取历史最高分。
     *
     * @return 历史最高分
     */
    public int getHighScore() {
        return highScore;
    }
}

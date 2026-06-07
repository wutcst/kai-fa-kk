/**
 * 该类是“芝麻开门”后端的游戏会话模型。
 * 游戏会话用于保存一局游戏运行过程中的玩家、地图、日志和当前游戏状态。
 *
 * GameSession 类本身只负责保存和查询状态，不直接实现移动、返回、拾取、使用物品等业务规则。
 * 后续服务层会基于该会话对象组合出完整游戏流程。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示当前正在运行的一局游戏。
 */
public class GameSession {

    private final String id;
    private final Player player;
    private final Map<String, Room> rooms;
    private final List<String> logs;
    private String previousRoomId;
    private GameStatus status;

    /**
     * 创建一局新的游戏会话。
     *
     * @param id 会话编号
     * @param player 当前玩家
     * @param rooms 游戏地图中的全部房间
     * @param status 初始游戏状态
     */
    public GameSession(String id, Player player, Map<String, Room> rooms, GameStatus status) {
        this.id = requireText(id, "会话编号不能为空");
        this.player = Objects.requireNonNull(player, "玩家不能为空");
        this.rooms = new LinkedHashMap<>(Objects.requireNonNull(rooms, "房间地图不能为空"));
        this.status = Objects.requireNonNull(status, "游戏状态不能为空");
        this.logs = new ArrayList<>();
        this.previousRoomId = null;
    }

    /**
     * 获取会话编号。
     *
     * @return 会话编号
     */
    public String getId() {
        return id;
    }

    /**
     * 获取当前玩家。
     *
     * @return 当前玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取全部房间映射。
     *
     * @return 只读房间映射，键为房间编号，值为房间对象
     */
    public Map<String, Room> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    /**
     * 根据房间编号查找房间。
     *
     * @param roomId 房间编号
     * @return 找到的房间，如果不存在则为空
     */
    public Optional<Room> findRoom(String roomId) {
        return Optional.ofNullable(rooms.get(requireText(roomId, "房间编号不能为空")));
    }

    /**
     * 获取玩家当前所在房间。
     *
     * @return 玩家当前所在房间
     */
    public Room getCurrentRoom() {
        String roomId = player.getCurrentRoomId();
        return findRoom(roomId).orElseThrow(() -> new IllegalStateException("当前房间不存在：" + roomId));
    }

    /**
     * 获取玩家上一次所在房间编号。
     *
     * @return 上一次所在房间编号，如果尚未移动则为空
     */
    public Optional<String> getPreviousRoomId() {
        return Optional.ofNullable(previousRoomId);
    }

    /**
     * 记录玩家上一次所在房间编号。
     *
     * @param previousRoomId 上一次所在房间编号
     */
    public void setPreviousRoomId(String previousRoomId) {
        this.previousRoomId = requireText(previousRoomId, "上一个房间编号不能为空");
    }

    /**
     * 获取游戏日志。
     *
     * @return 只读游戏日志列表
     */
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    /**
     * 添加一条游戏日志。
     *
     * @param log 日志内容
     */
    public void addLog(String log) {
        logs.add(requireText(log, "日志内容不能为空"));
    }

    /**
     * 获取当前游戏状态。
     *
     * @return 当前游戏状态
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * 修改当前游戏状态。
     *
     * @param status 新的游戏状态
     */
    public void setStatus(GameStatus status) {
        this.status = Objects.requireNonNull(status, "游戏状态不能为空");
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

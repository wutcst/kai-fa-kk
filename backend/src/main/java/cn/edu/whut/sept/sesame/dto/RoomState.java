/**
 * 该类是“芝麻开门”后端返回给前端的房间状态对象。
 * 房间状态对象用于展示当前房间名称、描述、出口、房间物品和特殊规则标记。
 *
 * RoomState 通常由 Room 模型转换得到，是前端房间展示区域的数据来源。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.dto;

import cn.edu.whut.sept.sesame.model.Room;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表示前端可见的房间状态。
 */
public class RoomState {

    private final String id;
    private final String name;
    private final String description;
    private final Map<String, String> exits;
    private final List<ItemState> items;
    private final boolean exit;
    private final boolean requiresPassword;

    /**
     * 创建一个房间状态对象。
     *
     * @param id 房间编号
     * @param name 房间名称
     * @param description 房间描述
     * @param exits 出口映射
     * @param items 房间物品状态列表
     * @param exit 是否为最终出口
     * @param requiresPassword 是否需要暗语
     */
    public RoomState(String id, String name, String description, Map<String, String> exits,
            List<ItemState> items, boolean exit, boolean requiresPassword) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exits = Map.copyOf(exits);
        this.items = List.copyOf(items);
        this.exit = exit;
        this.requiresPassword = requiresPassword;
    }

    /**
     * 根据房间模型创建房间状态对象。
     *
     * @param room 房间模型
     * @return 房间状态对象
     */
    public static RoomState from(Room room) {
        Objects.requireNonNull(room, "房间不能为空");
        List<ItemState> items = room.getItems().stream()
                .map(ItemState::from)
                .collect(Collectors.toList());
        return new RoomState(room.getId(), room.getName(), room.getDescription(), room.getExits(),
                items, room.isExit(), room.isRequiresPassword());
    }

    /**
     * 获取房间编号。
     *
     * @return 房间编号
     */
    public String getId() {
        return id;
    }

    /**
     * 获取房间名称。
     *
     * @return 房间名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取房间描述。
     *
     * @return 房间描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取出口映射。
     *
     * @return 出口映射
     */
    public Map<String, String> getExits() {
        return exits;
    }

    /**
     * 获取房间物品状态列表。
     *
     * @return 房间物品状态列表
     */
    public List<ItemState> getItems() {
        return items;
    }

    /**
     * 判断该房间是否为最终出口。
     *
     * @return 是最终出口时返回 true，否则返回 false
     */
    public boolean isExit() {
        return exit;
    }

    /**
     * 判断该房间是否需要暗语。
     *
     * @return 需要暗语时返回 true，否则返回 false
     */
    public boolean isRequiresPassword() {
        return requiresPassword;
    }
}

/**
 * 该类是“芝麻开门”后端的房间模型。
 * 房间模型用于描述地图中的地点，包括房间名称、描述、出口、房间物品和特殊规则标记。
 *
 * Room 类可以添加出口、查询出口、放置物品、移除物品，并标记该房间是否为出口房间
 * 或是否需要暗语才能进入。
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
 * 表示游戏地图中的一个房间。
 */
public class Room {

    private final String id;
    private final String name;
    private final String description;
    private final Map<String, String> exits;
    private final List<Item> items;
    private boolean exit;
    private boolean requiresPassword;

    /**
     * 创建一个普通房间。
     *
     * @param id 房间唯一编号
     * @param name 房间名称
     * @param description 房间描述
     */
    public Room(String id, String name, String description) {
        this(id, name, description, false, false);
    }

    /**
     * 创建一个带特殊规则标记的房间。
     *
     * @param id 房间唯一编号
     * @param name 房间名称
     * @param description 房间描述
     * @param exit 是否为出口房间
     * @param requiresPassword 是否需要暗语
     */
    public Room(String id, String name, String description, boolean exit, boolean requiresPassword) {
        this.id = requireText(id, "房间编号不能为空");
        this.name = requireText(name, "房间名称不能为空");
        this.description = Objects.requireNonNullElse(description, "");
        this.exit = exit;
        this.requiresPassword = requiresPassword;
        this.exits = new LinkedHashMap<>();
        this.items = new ArrayList<>();
    }

    /**
     * 获取房间唯一编号。
     *
     * @return 房间唯一编号
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
     * 获取房间出口映射。
     *
     * @return 只读出口映射，键为方向，值为目标房间编号
     */
    public Map<String, String> getExits() {
        return Collections.unmodifiableMap(exits);
    }

    /**
     * 获取房间内物品列表。
     *
     * @return 只读房间物品列表
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
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
     * 设置该房间是否为最终出口。
     *
     * @param exit 是否为最终出口
     */
    public void setExit(boolean exit) {
        this.exit = exit;
    }

    /**
     * 判断该房间是否需要暗语。
     *
     * @return 需要暗语时返回 true，否则返回 false
     */
    public boolean isRequiresPassword() {
        return requiresPassword;
    }

    /**
     * 设置该房间是否需要暗语。
     *
     * @param requiresPassword 是否需要暗语
     */
    public void setRequiresPassword(boolean requiresPassword) {
        this.requiresPassword = requiresPassword;
    }

    /**
     * 添加一个房间出口。
     *
     * @param direction 出口方向
     * @param targetRoomId 目标房间编号
     */
    public void addExit(String direction, String targetRoomId) {
        exits.put(normalizeDirection(direction), requireText(targetRoomId, "目标房间编号不能为空"));
    }

    /**
     * 根据方向获取目标房间编号。
     *
     * @param direction 出口方向
     * @return 目标房间编号，如果该方向没有出口则为空
     */
    public Optional<String> getExit(String direction) {
        return Optional.ofNullable(exits.get(normalizeDirection(direction)));
    }

    /**
     * 向房间中添加物品。
     *
     * @param item 待添加物品
     */
    public void addItem(Item item) {
        items.add(Objects.requireNonNull(item, "物品不能为空"));
    }

    /**
     * 根据物品编号移除房间中的物品。
     *
     * @param itemId 物品编号
     * @return 被移除的物品，如果不存在则为空
     */
    public Optional<Item> removeItem(String itemId) {
        requireText(itemId, "物品编号不能为空");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getId().equals(itemId)) {
                items.remove(i);
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * 根据物品编号查找房间中的物品。
     *
     * @param itemId 物品编号
     * @return 查找到的物品，如果不存在则为空
     */
    public Optional<Item> findItem(String itemId) {
        requireText(itemId, "物品编号不能为空");
        return items.stream().filter(item -> item.getId().equals(itemId)).findFirst();
    }

    private static String normalizeDirection(String direction) {
        return requireText(direction, "方向不能为空").trim().toLowerCase();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

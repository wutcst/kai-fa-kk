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
import cn.edu.whut.sept.sesame.dto.LeaderboardEntry;
import cn.edu.whut.sept.sesame.dto.ShopCatalogItem;
import cn.edu.whut.sept.sesame.model.GameSession;
import cn.edu.whut.sept.sesame.model.GameStatus;
import cn.edu.whut.sept.sesame.model.Player;
import cn.edu.whut.sept.sesame.persistence.SqliteGameStore;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 测试 GameService 服务。
 */
class GameServiceTest {

    @TempDir
    private Path tempDir;

    /**
     * 确认开始游戏后会创建玩家并进入初始房间。
     */
    @Test
    void startGameCreatesPlayerAtEntrance() {
        GameService service = createService();

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
        GameService service = createService();

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
        GameService service = createService();

        GameState state = service.startGame();
        GameSession session = service.getCurrentSession(state.getSessionId());

        assertEquals(28, session.getRooms().size());
        assertTrue(session.findRoom("stone-court").orElseThrow().getExit("east").isPresent());
        assertTrue(session.findRoom("supply-alcove").orElseThrow().getItems().size() >= 2);
        assertTrue(session.findRoom("moon-secret-room").isPresent());
        assertTrue(session.findRoom("final-gate").orElseThrow().isExit());
        assertEquals("mechanism-gallery", session.findRoom("final-gate").orElseThrow().getExit("west").orElseThrow());
    }

    /**
     * 确认 getState 可以返回当前房间、玩家、物品和日志。
     */
    @Test
    void getStateReturnsCurrentPlayerRoomItemsAndLogs() {
        GameService service = createService();

        GameState startedState = service.startGame();
        service.getCurrentSession(startedState.getSessionId()).getPlayer().moveTo("supply-alcove");
        GameState state = service.getState(startedState.getSessionId());

        assertEquals("supply-alcove", state.getCurrentRoom().getId());
        assertEquals("补给壁龛", state.getCurrentRoom().getName());
        assertFalse(state.getCurrentRoom().getItems().isEmpty());
        assertEquals("supply-alcove", state.getPlayer().getCurrentRoomId());
        assertFalse(state.getLogs().isEmpty());
    }

    /**
     * 确认合法移动会改变位置并扣除体力。
     */
    @Test
    void moveChangesRoomAndConsumesStamina() {
        GameService service = createService();
        GameState startedState = service.startGame();

        GameState movedState = service.move(startedState.getSessionId(), "east");

        assertEquals("stone-court", movedState.getPlayer().getCurrentRoomId());
        assertEquals("石门外庭", movedState.getCurrentRoom().getName());
        assertEquals(Player.DEFAULT_STAMINA - GameService.MOVE_STAMINA_COST, movedState.getPlayer().getStamina());
    }

    /**
     * 确认非法方向不会改变位置和体力。
     */
    @Test
    void moveWithInvalidDirectionDoesNotChangePlayerState() {
        GameService service = createService();
        GameState startedState = service.startGame();

        GameState movedState = service.move(startedState.getSessionId(), "north");

        assertEquals(GameService.START_ROOM_ID, movedState.getPlayer().getCurrentRoomId());
        assertEquals(Player.DEFAULT_STAMINA, movedState.getPlayer().getStamina());
        assertEquals("当前房间没有通向 north 的出口。", movedState.getMessage());
    }

    /**
     * 确认体力不足时会重新开始当前关。
     */
    @Test
    void moveRestartsCurrentLevelWhenStaminaIsNotEnough() {
        GameService service = createService();
        GameState startedState = service.startGame();
        service.getCurrentSession(startedState.getSessionId()).getPlayer()
                .decreaseStamina(Player.DEFAULT_STAMINA - 1);

        GameState movedState = service.move(startedState.getSessionId(), "east");

        assertEquals(GameService.START_ROOM_ID, movedState.getPlayer().getCurrentRoomId());
        assertEquals(Player.DEFAULT_STAMINA, movedState.getPlayer().getStamina());
        assertEquals(Player.DEFAULT_MONEY - GameService.TICKET_COST, movedState.getPlayer().getMoney());
        assertEquals("体力不足，当前关重新开始。", movedState.getMessage());
    }

    /**
     * 确认返回指令可以让玩家回到上一个房间并扣除体力。
     */
    @Test
    void backReturnsPlayerToPreviousRoomAndConsumesStamina() {
        GameService service = createService();
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
        GameService service = createService();
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
        GameService service = createService();
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
        GameService service = createService();
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
        GameService service = createService();
        GameState startedState = service.startGame();
        GameState supplyRoomState = service.move(startedState.getSessionId(), "east");
        supplyRoomState = service.move(supplyRoomState.getSessionId(), "south");
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
        GameService service = createService();
        GameState startedState = service.startGame();
        service.getCurrentSession(startedState.getSessionId()).getPlayer()
                .addItem(new cn.edu.whut.sept.sesame.model.Item("rope", "结实绳索",
                        "粗麻编成的绳索，可以把更多物品牢牢捆在背包外侧。",
                        cn.edu.whut.sept.sesame.model.ItemType.EQUIPMENT, 3, 0, 5, 0));

        GameState state = service.useItem(startedState.getSessionId(), "rope");

        assertEquals(Player.DEFAULT_MAX_WEIGHT + 5, state.getPlayer().getMaxWeight());
        assertTrue(state.getPlayer().getInventory().stream()
                .noneMatch(item -> item.getId().equals("rope")));
        assertEquals("你使用了结实绳索，最大负重增加 5。", state.getMessage());
    }

    /**
     * 确认未输入正确暗语时不能进入最终石门。
     */
    @Test
    void moveToPasswordRoomFailsBeforePasswordIsUnlocked() {
        GameService service = createService();
        GameState state = service.startGame();
        service.getCurrentSession(state.getSessionId()).getPlayer().moveTo("mechanism-gallery");

        GameState blockedState = service.move(state.getSessionId(), "east");

        assertEquals("mechanism-gallery", blockedState.getPlayer().getCurrentRoomId());
        assertEquals("最终石门需要正确暗语才能进入。", blockedState.getMessage());
    }

    /**
     * 确认正确暗语可以解锁宝库和最终出口。
     */
    @Test
    void submitPasswordUnlocksPasswordRooms() {
        GameService service = createService();
        GameState state = service.startGame();
        service.getCurrentSession(state.getSessionId()).getPlayer().moveTo("mechanism-gallery");

        GameState unlockedState = service.submitPassword(state.getSessionId(), GameService.CORRECT_PASSWORD);

        assertTrue(unlockedState.isPasswordUnlocked());
        assertEquals("暗语正确，最终石门的纹路亮了起来。", unlockedState.getMessage());
    }

    /**
     * 确认不在机关长廊时不能输入暗语。
     */
    @Test
    void submitPasswordFailsOutsideMechanismGallery() {
        GameService service = createService();
        GameState state = service.startGame();

        GameState blockedState = service.submitPassword(state.getSessionId(), GameService.CORRECT_PASSWORD);

        assertFalse(blockedState.isPasswordUnlocked());
        assertEquals("这里没有可以输入暗语的机关。", blockedState.getMessage());
    }

    /**
     * 确认带着宝物到达最终出口时可以通关。
     */
    @Test
    void playerWinsWhenReachingExitWithTreasureAfterPasswordUnlocked() {
        GameService service = createService();
        GameState state = service.startGame();
        service.getCurrentSession(state.getSessionId()).getPlayer().moveTo("mechanism-gallery");
        service.submitPassword(state.getSessionId(), GameService.CORRECT_PASSWORD);
        service.getCurrentSession(state.getSessionId()).getPlayer()
                .addItem(new cn.edu.whut.sept.sesame.model.Item("gold-crown", "沉金王冠",
                        "王冠沉重冰冷，内侧刻着早已失落的王名。",
                        cn.edu.whut.sept.sesame.model.ItemType.TREASURE, 6, 0, 0, 180));

        GameState wonState = service.move(state.getSessionId(), "east");

        assertEquals(GameStatus.WON, wonState.getStatus());
        assertEquals("final-gate", wonState.getPlayer().getCurrentRoomId());
        assertTrue(wonState.getFinalScore() > 0);
    }

    /**
     * 确认可以手动重新开始当前关。
     */
    @Test
    void restartLevelRestoresCurrentLevelCheckpoint() {
        GameService service = createService();
        GameState state = service.startGame();
        state = service.move(state.getSessionId(), "east");

        GameState restartedState = service.restartLevel(state.getSessionId());

        assertEquals(GameStatus.IN_PROGRESS, restartedState.getStatus());
        assertEquals(GameService.START_ROOM_ID, restartedState.getPlayer().getCurrentRoomId());
        assertEquals("当前关已重新开始。", restartedState.getMessage());
    }

    /**
     * 确认账号可以注册、登录，并通过 SQLite 保存和读取游戏。
     */
    @Test
    void registerLoginSaveAndLoadGameWithSqlite() {
        SqliteGameStore store = new SqliteGameStore(tempDir.resolve("test-save.db"));
        GameService service = new GameService(store);
        assertTrue(service.register("kay", "123456").isSuccess());
        String token = service.login("kay", "123456").getToken();
        assertFalse(token.isBlank());
        GameState state = service.startGameWithToken(token);
        state = service.move(state.getSessionId(), "east");
        service.getCurrentSession(state.getSessionId()).getPlayer().moveTo("mechanism-gallery");
        service.submitPassword(state.getSessionId(), GameService.CORRECT_PASSWORD);
        service.getCurrentSession(state.getSessionId()).getPlayer().moveTo("stone-court");
        service.saveGame(token, state.getSessionId());

        GameService reloadedService = new GameService(store);
        String reloadedToken = reloadedService.login("kay", "123456").getToken();
        GameState loadedState = reloadedService.loadGame(reloadedToken);

        assertEquals("stone-court", loadedState.getPlayer().getCurrentRoomId());
        assertTrue(loadedState.isPasswordUnlocked());
    }

    /**
     * 确认玩家不能保存其他账号创建的游戏会话。
     */
    @Test
    void saveGameRejectsOtherPlayersSession() {
        GameService service = createService();
        assertTrue(service.register("kay-a", "123456").isSuccess());
        assertTrue(service.register("kay-b", "123456").isSuccess());
        String tokenA = service.login("kay-a", "123456").getToken();
        String tokenB = service.login("kay-b", "123456").getToken();
        GameState state = service.startGameWithToken(tokenA);

        assertThrows(IllegalStateException.class, () -> service.saveGame(tokenB, state.getSessionId()));
    }

    /**
     * 确认游戏尚未开始时不能直接读取状态。
     */
    @Test
    void getStateBeforeStartThrowsException() {
        GameService service = createService();

        assertThrows(IllegalStateException.class, () -> service.getState("missing-session"));
    }

    /**
     * 确认商店目录仅在商店阶段可查询，并与购买价格使用同一来源。
     */
    @Test
    void shopCatalogIsStableAndMatchesPurchasePrice() {
        GameService service = createService();
        GameState state = service.startGame();

        assertThrows(IllegalStateException.class, () -> service.listShopCatalog(state.getSessionId()));

        service.getCurrentSession(state.getSessionId()).setStatus(GameStatus.SHOPPING);
        List<ShopCatalogItem> catalog = service.listShopCatalog(state.getSessionId());

        assertEquals(7, catalog.size());
        assertEquals(List.of("clean-water", "dry-food", "stamina-potion", "rope", "iron-boots",
                "lockpick", "lantern"), catalog.stream().map(ShopCatalogItem::getItemId).toList());
        assertEquals(List.of(10, 8, 18, 20, 25, 25, 15),
                catalog.stream().map(ShopCatalogItem::getPrice).toList());
        assertFalse(catalog.get(0).getName().isBlank());
        assertFalse(catalog.get(0).getDescription().isBlank());
        assertEquals(2, catalog.get(0).getWeight());
        assertEquals(10, catalog.get(0).getStaminaEffect());

        int moneyBeforePurchase = state.getPlayer().getMoney();
        GameState purchasedState = service.buyItem(state.getSessionId(), catalog.get(0).getItemId());
        assertEquals(moneyBeforePurchase - catalog.get(0).getPrice(), purchasedState.getPlayer().getMoney());
    }

    /**
     * 确认排行榜过滤零分用户，并按分数和用户名稳定排序。
     */
    @Test
    void leaderboardFiltersSortsAndLimitsHighScores() {
        SqliteGameStore store = new SqliteGameStore(tempDir.resolve("leaderboard.db"));
        GameService service = new GameService(store);
        service.register("zero-player", "123456");
        service.register("charlie", "123456");
        service.register("alice", "123456");
        service.register("bob", "123456");
        store.updateHighScore("charlie", 80);
        store.updateHighScore("alice", 120);
        store.updateHighScore("bob", 120);

        List<LeaderboardEntry> entries = service.listLeaderboard(2);

        assertEquals(2, entries.size());
        assertEquals("alice", entries.get(0).getUsername());
        assertEquals(120, entries.get(0).getScore());
        assertEquals(1, entries.get(0).getRank());
        assertEquals("bob", entries.get(1).getUsername());
        assertEquals(2, entries.get(1).getRank());
        assertTrue(entries.stream().noneMatch(entry -> entry.getUsername().equals("zero-player")));
        assertThrows(IllegalArgumentException.class, () -> service.listLeaderboard(0));
        assertThrows(IllegalArgumentException.class, () -> service.listLeaderboard(101));
    }

    /**
     * 确认没有有效成绩时排行榜为空。
     */
    @Test
    void leaderboardIsEmptyWithoutPositiveScores() {
        GameService service = createService();
        service.register("zero-player", "123456");

        assertTrue(service.listLeaderboard(10).isEmpty());
    }

    private GameService createService() {
        Path databasePath = tempDir.resolve("service-test-" + UUID.randomUUID() + ".db");
        return new GameService(new SqliteGameStore(databasePath));
    }
}

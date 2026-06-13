/**
 * 该类是“芝麻开门”后端游戏控制器的测试类。
 * 测试内容包括开始游戏接口和读取游戏状态接口。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import cn.edu.whut.sept.sesame.persistence.SqliteGameStore;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 测试 GameController 控制器。
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "sesame.database.path=target/test-data/controller-test.db")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SqliteGameStore gameStore;

    /**
     * 确认开始游戏接口可以返回初始游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void startGameReturnsInitialState() throws Exception {
        mockMvc.perform(post("/api/game/start").param("token", registerAndLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.player.currentRoomId").value("entrance"))
                .andExpect(jsonPath("$.canSubmitPassword").value(false))
                .andExpect(jsonPath("$.currentRoom.name").value("秘窟入口"));
    }

    /**
     * 确认读取状态接口可以返回当前游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void getStateReturnsCurrentStateAfterStart() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");

        mockMvc.perform(get("/api/game/state").param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.currentRoom.id").value("entrance"))
                .andExpect(jsonPath("$.logs").isArray());
    }

    /**
     * 确认未知会话读取状态时会返回冲突状态码。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void getStateReturnsConflictWhenSessionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/game/state").param("sessionId", "missing-session"))
                .andExpect(status().isConflict());
    }

    /**
     * 确认移动接口可以返回移动后的游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void moveReturnsStateAfterPlayerMoves() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");

        mockMvc.perform(post("/api/game/move")
                        .param("sessionId", sessionId)
                        .param("direction", "east"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoom.id").value("stone-court"))
                .andExpect(jsonPath("$.player.stamina").value(25));
    }

    /**
     * 确认返回接口可以返回上一个房间。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void backReturnsPreviousRoom() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");
        mockMvc.perform(post("/api/game/move")
                        .param("sessionId", sessionId)
                        .param("direction", "east"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/back").param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoom.id").value("entrance"))
                .andExpect(jsonPath("$.player.stamina").value(20));
    }

    /**
     * 确认拾取接口可以把物品加入玩家背包。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void takeItemAddsItemToInventory() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");

        mockMvc.perform(post("/api/game/take")
                        .param("sessionId", sessionId)
                        .param("itemId", "old-map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.inventory[0].id").value("old-map"))
                .andExpect(jsonPath("$.player.currentWeight").value(1));
    }

    /**
     * 确认丢弃接口可以把背包物品放回当前房间。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void dropItemReturnsItemToCurrentRoom() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");
        mockMvc.perform(post("/api/game/take")
                        .param("sessionId", sessionId)
                        .param("itemId", "old-map"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/drop")
                        .param("sessionId", sessionId)
                        .param("itemId", "old-map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.inventory").isEmpty())
                .andExpect(jsonPath("$.currentRoom.items[0].id").value("old-map"));
    }

    /**
     * 确认使用接口可以应用补给效果。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void useItemAppliesSupplyEffect() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");
        mockMvc.perform(post("/api/game/move")
                        .param("sessionId", sessionId)
                        .param("direction", "east"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/game/move")
                        .param("sessionId", sessionId)
                        .param("direction", "south"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/game/take")
                        .param("sessionId", sessionId)
                        .param("itemId", "clean-water"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/use")
                        .param("sessionId", sessionId)
                        .param("itemId", "clean-water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.player.stamina").value(30))
                .andExpect(jsonPath("$.player.inventory").isEmpty());
    }

    /**
     * 确认账号注册和登录接口可以返回成功结果。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void authRegisterAndLoginReturnSuccess() throws Exception {
        String username = "controller-user-" + UUID.randomUUID();

        mockMvc.perform(post("/api/auth/register")
                        .param("username", username)
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/auth/login")
                        .param("username", username)
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    /**
     * 确认暗语接口可以解锁游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void passwordApiRejectsPasswordOutsideMechanismGallery() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");

        mockMvc.perform(post("/api/game/password")
                        .param("sessionId", sessionId)
                        .param("password", "芝麻开门"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordUnlocked").value(false))
                .andExpect(jsonPath("$.message").value("这里没有可以输入暗语的机关。"));
    }

    /**
     * 确认保存和读取接口可以通过 SQLite 恢复游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void saveAndLoadApiRestoresGameState() throws Exception {
        String username = "save-user-" + UUID.randomUUID();
        mockMvc.perform(post("/api/auth/register")
                        .param("username", username)
                        .param("password", "123456"))
                .andExpect(status().isOk());
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .param("username", username)
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andReturn();
        String token = JsonPath.read(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.token");
        MvcResult startResult = mockMvc.perform(post("/api/game/start").param("token", token))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");
        mockMvc.perform(post("/api/game/move")
                        .param("sessionId", sessionId)
                        .param("direction", "east"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/save")
                        .param("token", token)
                        .param("sessionId", sessionId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/load").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoom.id").value("stone-court"));
    }

    /**
     * 确认商店目录接口限制商店状态并返回完整稳定目录。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void shopCatalogRequiresShoppingStateAndReturnsCatalog() throws Exception {
        MvcResult startResult = startGameWithLogin();
        String sessionId = JsonPath.read(
                startResult.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.sessionId");

        mockMvc.perform(get("/api/game/shop/catalog").param("sessionId", sessionId))
                .andExpect(status().isConflict());

        move(sessionId, "east");
        move(sessionId, "east");
        move(sessionId, "south");
        move(sessionId, "south");
        move(sessionId, "south");

        mockMvc.perform(get("/api/game/shop/catalog").param("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].itemId").value("clean-water"))
                .andExpect(jsonPath("$[0].price").value(10))
                .andExpect(jsonPath("$[0].name").isNotEmpty())
                .andExpect(jsonPath("$[0].description").isNotEmpty())
                .andExpect(jsonPath("$[0].type").value("SUPPLY"))
                .andExpect(jsonPath("$[0].weight").value(2))
                .andExpect(jsonPath("$[0].staminaEffect").value(10))
                .andExpect(jsonPath("$[0].maxWeightEffect").value(0))
                .andExpect(jsonPath("$[0].moneyValue").value(0))
                .andExpect(jsonPath("$[6].itemId").value("lantern"))
                .andExpect(jsonPath("$[6].price").value(15));
    }

    /**
     * 确认排行榜接口支持稳定排序、limit 和非法参数校验。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void leaderboardReturnsSortedEntriesAndRejectsInvalidLimit() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String firstUsername = "leader-a-" + suffix;
        String secondUsername = "leader-b-" + suffix;
        String zeroUsername = "leader-zero-" + suffix;
        registerUser(firstUsername);
        registerUser(secondUsername);
        registerUser(zeroUsername);
        gameStore.updateHighScore(firstUsername, Integer.MAX_VALUE);
        gameStore.updateHighScore(secondUsername, Integer.MAX_VALUE);

        mockMvc.perform(get("/api/game/leaderboard").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].username").isNotEmpty())
                .andExpect(jsonPath("$[0].score").isNumber())
                .andExpect(jsonPath("$[1].rank").value(2))
                .andExpect(jsonPath("$[1].username").isNotEmpty())
                .andExpect(jsonPath("$[1].score").isNumber());

        mockMvc.perform(get("/api/game/leaderboard").param("limit", "0"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/game/leaderboard").param("limit", "101"))
                .andExpect(status().isConflict());
    }

    private void move(String sessionId, String direction) throws Exception {
        mockMvc.perform(post("/api/game/move")
                        .param("sessionId", sessionId)
                        .param("direction", direction))
                .andExpect(status().isOk());
    }

    private void registerUser(String username) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .param("username", username)
                        .param("password", "123456"))
                .andExpect(status().isOk());
    }

    private MvcResult startGameWithLogin() throws Exception {
        return mockMvc.perform(post("/api/game/start").param("token", registerAndLogin()))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String registerAndLogin() throws Exception {
        String username = "controller-user-" + UUID.randomUUID();
        mockMvc.perform(post("/api/auth/register")
                        .param("username", username)
                        .param("password", "123456"))
                .andExpect(status().isOk());
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .param("username", username)
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.token");
    }
}

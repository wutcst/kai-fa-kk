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
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 测试 GameController 控制器。
 */
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 确认开始游戏接口可以返回初始游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void startGameReturnsInitialState() throws Exception {
        mockMvc.perform(post("/api/game/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.player.currentRoomId").value("entrance"))
                .andExpect(jsonPath("$.currentRoom.name").value("秘窟入口"));
    }

    /**
     * 确认读取状态接口可以返回当前游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void getStateReturnsCurrentStateAfterStart() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/api/game/start"))
                .andExpect(status().isOk())
                .andReturn();
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
        MvcResult startResult = mockMvc.perform(post("/api/game/start"))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = startResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String sessionId = JsonPath.read(responseBody, "$.sessionId");

        mockMvc.perform(post("/api/game/move")
                        .param("sessionId", sessionId)
                        .param("direction", "east"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRoom.id").value("stone-hall"))
                .andExpect(jsonPath("$.player.stamina").value(25));
    }
}

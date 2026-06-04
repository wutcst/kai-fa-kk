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

import cn.edu.whut.sept.sesame.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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
                .andExpect(jsonPath("$.sessionId").value(GameService.DEFAULT_SESSION_ID))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.player.currentRoomId").value(GameService.START_ROOM_ID))
                .andExpect(jsonPath("$.currentRoom.name").value("秘窟入口"));
    }

    /**
     * 确认读取状态接口可以返回当前游戏状态。
     *
     * @throws Exception MockMvc 请求异常
     */
    @Test
    void getStateReturnsCurrentStateAfterStart() throws Exception {
        mockMvc.perform(post("/api/game/start"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/game/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(GameService.DEFAULT_SESSION_ID))
                .andExpect(jsonPath("$.currentRoom.id").value(GameService.START_ROOM_ID))
                .andExpect(jsonPath("$.logs").isArray());
    }
}

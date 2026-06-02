/**
 * 该类是“芝麻开门”后端健康检查接口的测试类。
 * 该测试用于验证控制器能够正确处理 {@code /api/health} 请求，并返回约定的状态数据。
 *
 * 如果想确认前后端联调前的后端基础接口是否正常，可以优先查看该测试是否通过。
 *
 * HealthControllerTest 类通过 MockMvc 模拟 HTTP 请求，避免启动真实服务器即可测试控制器行为。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    private final MockMvc mockMvc;

    /**
     * 创建健康检查接口测试对象。
     *
     * @param mockMvc 用于模拟调用控制器接口的 MVC 测试客户端
     */
    @Autowired
    HealthControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    /**
     * 确认健康检查接口返回 HTTP 200 和 ok 状态。
     *
     * @throws Exception 模拟请求执行失败时抛出
     */
    @Test
    void healthReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
}

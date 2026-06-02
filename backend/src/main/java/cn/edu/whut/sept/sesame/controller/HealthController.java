/**
 * 该类是“芝麻开门”后端的健康检查控制器。
 * 健康检查接口用于确认后端服务是否已经启动，并为前端联调和自动化测试提供最小可用 API。
 *
 * 如果想验证后端是否可用，可以在服务启动后访问 {@code /api/health} 接口。
 *
 * HealthController 类负责接收健康检查请求，并返回简单的 JSON 状态数据。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * 返回简单的健康检查结果，用于本地开发和自动化测试确认服务可用。
     *
     * @return 包含 {@code ok} 状态值的健康检查响应
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}

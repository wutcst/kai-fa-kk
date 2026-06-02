/**
 * 该类是“芝麻开门”后端应用程序的启动测试类。
 * 该测试用于验证 Spring Boot 应用上下文能够正常加载，避免基础配置错误导致项目无法启动。
 *
 * 如果该测试失败，通常说明启动类、依赖配置或 Spring 组件扫描存在问题。
 *
 * SesameApplicationTests 类通过加载完整应用上下文，检查后端工程骨架是否具备基本可运行性。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SesameApplicationTests {

    /**
     * 加载 Spring 应用上下文。
     */
    @Test
    void contextLoads() {
    }
}

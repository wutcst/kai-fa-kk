/**
 * 该类是“芝麻开门”后端应用程序的主类。
 * 《芝麻开门》后端基于 Spring Boot 构建，负责为前端图形化文字冒险游戏提供 REST 接口。
 *
 * 如果想开始执行后端服务，可以运行该类的 main 方法，或者在 backend 目录下执行
 * {@code mvn spring-boot:run} 命令。
 *
 * SesameApplication 类的启动过程将初始化 Spring 应用上下文，并自动扫描控制器、服务、
 * 模型等后端组件。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SesameApplication {

    /**
     * 启动 Spring Boot 后端应用。
     *
     * @param args 启动时传入的命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SesameApplication.class, args);
    }
}

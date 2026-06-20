/**
 * 该类是“芝麻开门”后端的账号控制器。
 * 账号控制器用于处理本地试玩和前端页面发起的注册、登录请求。
 *
 * AuthController 不直接处理游戏规则，只把账号请求交给 GameService 中的账号相关方法。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.controller;

import cn.edu.whut.sept.sesame.dto.AuthResult;
import cn.edu.whut.sept.sesame.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 提供账号注册和登录 REST 接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final GameService gameService;

    /**
     * 创建账号控制器。
     *
     * @param gameService 游戏服务
     */
    public AuthController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * 注册玩家账号。
     *
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
     */
    @PostMapping("/register")
    public AuthResult register(@RequestParam String username, @RequestParam String password) {
        try {
            return gameService.register(username, password);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 登录玩家账号。
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/login")
    public AuthResult login(@RequestParam String username, @RequestParam String password) {
        try {
            return gameService.login(username, password);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }
}

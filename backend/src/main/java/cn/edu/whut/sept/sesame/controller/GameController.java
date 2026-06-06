/**
 * 该类是“芝麻开门”后端的游戏控制器。
 * 游戏控制器用于把前端或本地调试请求转发给游戏服务层，并返回当前游戏状态。
 *
 * 如果想在不运行前端页面的情况下验证 Issue #7，可以启动后端后访问
 * {@code /api/game/start} 和 {@code /api/game/state} 接口查看 JSON 结果。
 *
 * GameController 当前只提供开始游戏和读取状态接口，移动、拾取、使用物品等操作会在后续
 * Issue 中继续补充。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.controller;

import cn.edu.whut.sept.sesame.dto.GameState;
import cn.edu.whut.sept.sesame.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 提供游戏会话相关 REST 接口。
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    /**
     * 创建游戏控制器。
     *
     * @param gameService 游戏服务
     */
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * 开始一局新游戏。
     *
     * @return 初始游戏状态
     */
    @PostMapping("/start")
    public GameState startGame() {
        return gameService.startGame();
    }

    /**
     * 获取当前游戏状态。
     *
     * @param sessionId 会话编号
     * @return 当前游戏状态
     */
    @GetMapping("/state")
    public GameState getState(@RequestParam String sessionId) {
        try {
            return gameService.getState(sessionId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }
}

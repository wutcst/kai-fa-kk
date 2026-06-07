/**
 * 该类是“芝麻开门”后端的游戏控制器。
 * 游戏控制器用于把前端或本地调试请求转发给游戏服务层，并返回当前游戏状态。
 *
 * 如果想在不运行前端页面的情况下验证 Issue #7，可以启动后端后访问
 * {@code /api/game/start} 和 {@code /api/game/state} 接口查看 JSON 结果。
 *
 * GameController 当前提供开始游戏、读取状态、移动、返回、拾取、丢弃和使用物品接口。
 * 登录、存档、暗语、救援和通关等操作会在后续 Issue 中继续补充。
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

    /**
     * 按指定方向移动玩家。
     *
     * @param sessionId 会话编号
     * @param direction 移动方向
     * @return 移动后的游戏状态
     */
    @PostMapping("/move")
    public GameState move(@RequestParam String sessionId, @RequestParam String direction) {
        try {
            return gameService.move(sessionId, direction);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 返回玩家上一次所在房间。
     *
     * @param sessionId 会话编号
     * @return 返回后的游戏状态
     */
    @PostMapping("/back")
    public GameState back(@RequestParam String sessionId) {
        try {
            return gameService.back(sessionId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 拾取当前房间中的指定物品。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 拾取后的游戏状态
     */
    @PostMapping("/take")
    public GameState takeItem(@RequestParam String sessionId, @RequestParam String itemId) {
        try {
            return gameService.takeItem(sessionId, itemId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 丢弃背包中的指定物品到当前房间。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 丢弃后的游戏状态
     */
    @PostMapping("/drop")
    public GameState dropItem(@RequestParam String sessionId, @RequestParam String itemId) {
        try {
            return gameService.dropItem(sessionId, itemId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 使用背包中的指定物品。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 使用后的游戏状态
     */
    @PostMapping("/use")
    public GameState useItem(@RequestParam String sessionId, @RequestParam String itemId) {
        try {
            return gameService.useItem(sessionId, itemId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }
}

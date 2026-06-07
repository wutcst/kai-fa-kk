/**
 * 该类是“芝麻开门”后端的游戏控制器。
 * 游戏控制器用于把前端或本地调试请求转发给游戏服务层，并返回当前游戏状态。
 *
 * 如果想在不运行前端页面的情况下验证游戏接口，需要先调用登录接口获取 token，
 * 再访问 {@code /api/game/start} 和 {@code /api/game/state} 接口查看 JSON 结果。
 *
 * GameController 当前提供开始游戏、读取状态、移动、返回、拾取、丢弃、使用物品、暗语和存档接口。
 * 注册和登录接口由 AuthController 提供。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.controller;

import cn.edu.whut.sept.sesame.dto.GameState;
import cn.edu.whut.sept.sesame.dto.GameSaveSummary;
import cn.edu.whut.sept.sesame.service.GameService;
import java.util.List;
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
     * 使用登录令牌开始一局新游戏。
     *
     * @param token 登录令牌
     * @return 初始游戏状态
     */
    @PostMapping("/start")
    public GameState startGame(@RequestParam String token) {
        try {
            return gameService.startGameWithToken(token);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
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

    /**
     * 在商店购买指定物品。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 购买后的游戏状态
     */
    @PostMapping("/shop/buy")
    public GameState buyItem(@RequestParam String sessionId, @RequestParam String itemId) {
        try {
            return gameService.buyItem(sessionId, itemId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 在商店出售背包中的宝物。
     *
     * @param sessionId 会话编号
     * @param itemId 物品编号
     * @return 出售后的游戏状态
     */
    @PostMapping("/shop/sell")
    public GameState sellItem(@RequestParam String sessionId, @RequestParam String itemId) {
        try {
            return gameService.sellItem(sessionId, itemId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 从商店继续进入下一关。
     *
     * @param sessionId 会话编号
     * @return 进入下一关后的游戏状态
     */
    @PostMapping("/shop/continue")
    public GameState continueAdventure(@RequestParam String sessionId) {
        try {
            return gameService.continueAdventure(sessionId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 重新开始当前关。
     *
     * @param sessionId 会话编号
     * @return 重新开始后的游戏状态
     */
    @PostMapping("/restart-level")
    public GameState restartLevel(@RequestParam String sessionId) {
        try {
            return gameService.restartLevel(sessionId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 放弃本次探险并删除当前存档。
     *
     * @param sessionId 会话编号
     * @return 放弃后的游戏状态
     */
    @PostMapping("/abandon")
    public GameState abandonAdventure(@RequestParam String sessionId) {
        try {
            return gameService.abandonAdventure(sessionId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 提交暗语，正确时解锁宝库和最终出口。
     *
     * @param sessionId 会话编号
     * @param password 暗语
     * @return 提交暗语后的游戏状态
     */
    @PostMapping("/password")
    public GameState submitPassword(@RequestParam String sessionId, @RequestParam String password) {
        try {
            return gameService.submitPassword(sessionId, password);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 将当前游戏保存到 SQLite。
     *
     * @param token 登录令牌
     * @param sessionId 会话编号
     * @return 保存后的游戏状态
     */
    @PostMapping("/save")
    public GameState saveGame(@RequestParam String token, @RequestParam String sessionId,
            @RequestParam(required = false) String saveName) {
        try {
            return gameService.saveGame(token, sessionId, saveName);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 使用登录令牌从 SQLite 读取当前用户的游戏存档。
     *
     * @param token 登录令牌
     * @return 读取后的游戏状态
     */
    @PostMapping("/load")
    public GameState loadGame(@RequestParam String token, @RequestParam(required = false) Long saveId) {
        try {
            if (saveId == null) {
                return gameService.loadGame(token);
            }
            return gameService.loadGame(token, saveId);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }

    /**
     * 查询当前登录用户的存档列表。
     *
     * @param token 登录令牌
     * @return 存档摘要列表
     */
    @GetMapping("/saves")
    public List<GameSaveSummary> listGameSaves(@RequestParam String token) {
        try {
            return gameService.listGameSaves(token);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        }
    }
}

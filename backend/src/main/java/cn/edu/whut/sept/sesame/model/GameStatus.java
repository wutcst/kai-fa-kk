/**
 * 该枚举定义“芝麻开门”后端中的游戏状态。
 * 游戏状态用于描述当前会话是正在进行、已经通关，还是已经失败。
 *
 * GameStatus 枚举会被 GameSession 和 GameState 使用，后续移动、救援、通关等规则
 * 可以通过修改该状态控制前端展示和业务流程。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.model;

/**
 * 表示一局游戏当前所处的阶段。
 */
public enum GameStatus {

    /**
     * 游戏正在进行。
     */
    IN_PROGRESS,

    /**
     * 玩家已经完成通关目标。
     */
    WON,

    /**
     * 玩家已经触发失败条件。
     */
    FAILED
}

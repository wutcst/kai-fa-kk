/**
 * 该类是“芝麻开门”后端返回给本地调试或前端的账号操作结果。
 * 账号操作结果用于描述注册、登录等用户相关操作是否成功。
 *
 * AuthResult 只承载用户名、成功标记、提示消息和登录令牌，不暴露密码或密码哈希。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.dto;

/**
 * 表示一次账号操作的返回结果。
 */
public class AuthResult {

    private final String username;
    private final boolean success;
    private final String message;
    private final String token;

    /**
     * 创建账号操作结果。
     *
     * @param username 用户名
     * @param success 是否成功
     * @param message 提示消息
     * @param token 登录令牌，注册失败或登录失败时可以为空
     */
    public AuthResult(String username, boolean success, String message, String token) {
        this.username = username;
        this.success = success;
        this.message = message;
        this.token = token;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 判断账号操作是否成功。
     *
     * @return 成功时返回 true，否则返回 false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取账号操作提示消息。
     *
     * @return 提示消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取登录令牌。
     *
     * @return 登录令牌，未登录成功时为空
     */
    public String getToken() {
        return token;
    }
}

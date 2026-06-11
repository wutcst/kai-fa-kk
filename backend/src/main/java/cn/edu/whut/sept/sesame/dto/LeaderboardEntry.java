/**
 * 排行榜条目数据传输对象。
 */
package cn.edu.whut.sept.sesame.dto;

/**
 * 表示排行榜中的一名玩家。
 */
public class LeaderboardEntry {

    private final int rank;
    private final String username;
    private final int score;

    /**
     * 创建排行榜条目。
     *
     * @param rank 排名
     * @param username 用户名
     * @param score 历史最高分
     */
    public LeaderboardEntry(int rank, String username, int score) {
        this.rank = rank;
        this.username = username;
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
}

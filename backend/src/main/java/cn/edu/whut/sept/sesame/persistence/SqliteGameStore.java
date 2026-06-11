/**
 * 该类是“芝麻开门”后端的 SQLite 存储组件。
 * SQLite 存储组件负责创建账号表、存档主表和存档明细表，并提供账号、存档、高分相关读写能力。
 *
 * SqliteGameStore 只处理持久化，不实现游戏规则；游戏规则仍由 GameService 负责。
 *
 * @author 谢恺燊
 * @version 1.0
 */
package cn.edu.whut.sept.sesame.persistence;

import cn.edu.whut.sept.sesame.dto.GameSaveSummary;
import cn.edu.whut.sept.sesame.dto.LeaderboardEntry;
import cn.edu.whut.sept.sesame.model.GameStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 提供 SQLite 账号和存档读写能力。
 */
@Service
public class SqliteGameStore {

    private final Path databasePath;

    /**
     * 使用默认数据库路径创建 SQLite 存储组件。
     */
    public SqliteGameStore() {
        this(Path.of("data", "sesame-open.db"));
    }

    /**
     * 使用配置文件中的数据库路径创建 SQLite 存储组件。
     *
     * @param databasePath 数据库文件路径
     */
    @Autowired
    public SqliteGameStore(@Value("${sesame.database.path:data/sesame-open.db}") String databasePath) {
        this(Path.of(databasePath));
    }

    /**
     * 使用指定数据库路径创建 SQLite 存储组件。
     *
     * @param databasePath 数据库文件路径
     */
    public SqliteGameStore(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath();
        initializeDatabase();
    }

    /**
     * 注册新用户。
     *
     * @param username 用户名
     * @param passwordHash 密码哈希
     * @return 注册成功时返回 true，用户名已存在时返回 false
     */
    public boolean registerUser(String username, String passwordHash) {
        if (findPasswordHash(username).isPresent()) {
            return false;
        }
        String sql = "INSERT INTO users(username, password_hash, created_at) VALUES (?, ?, ?)";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, Instant.now().toString());
            statement.executeUpdate();
            return true;
        } catch (SQLException exception) {
            throw new IllegalStateException("注册用户失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 查询用户密码哈希。
     *
     * @param username 用户名
     * @return 密码哈希，如果用户不存在则为空
     */
    public Optional<String> findPasswordHash(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getString("password_hash"));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询用户失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 查询玩家历史最高分。
     *
     * @param username 用户名
     * @return 历史最高分，用户不存在时返回 0
     */
    public int findHighScore(String username) {
        String sql = "SELECT high_score FROM users WHERE username = ?";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("high_score");
                }
                return 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("查询最高分失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 在本次分数更高时更新玩家最高分。
     *
     * @param username 用户名
     * @param score 本次分数
     * @return 更新后的最高分
     */
    public int updateHighScore(String username, int score) {
        int currentHighScore = findHighScore(username);
        if (score <= currentHighScore) {
            return currentHighScore;
        }
        String sql = "UPDATE users SET high_score = ? WHERE username = ?";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, score);
            statement.setString(2, username);
            statement.executeUpdate();
            return score;
        } catch (SQLException exception) {
            throw new IllegalStateException("更新最高分失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 查询历史最高分排行榜。
     *
     * @param limit 返回条目数量上限
     * @return 按最高分降序、用户名升序排列的排行榜
     */
    public List<LeaderboardEntry> listTopHighScores(int limit) {
        String sql = """
                SELECT username, high_score
                FROM users
                WHERE high_score > 0
                ORDER BY high_score DESC, username ASC
                LIMIT ?
                """;
        List<LeaderboardEntry> entries = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                int rank = 1;
                while (resultSet.next()) {
                    entries.add(new LeaderboardEntry(rank, resultSet.getString("username"),
                            resultSet.getInt("high_score")));
                    rank++;
                }
            }
            return entries;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询排行榜失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 新建或覆盖游戏存档。
     *
     * @param record 游戏存档记录
     * @return 存档编号
     */
    public long saveGame(GameSaveRecord record) {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            long saveId = saveMainRecord(connection, record);
            replaceInventoryItems(connection, saveId, record.getInventoryItems());
            replaceRoomItems(connection, saveId, record.getRoomItems());
            replaceLogs(connection, saveId, record.getLogs());
            connection.commit();
            return saveId;
        } catch (SQLException exception) {
            throw new IllegalStateException("保存游戏失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 删除指定存档。
     *
     * @param username 用户名
     * @param saveId 存档编号
     */
    public void deleteGameSave(String username, long saveId) {
        String sql = "DELETE FROM game_saves WHERE username = ? AND save_id = ?";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setLong(2, saveId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("删除游戏存档失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 删除指定用户的所有存档。
     *
     * @param username 用户名
     */
    public void deleteAllGameSaves(String username) {
        String sql = "DELETE FROM game_saves WHERE username = ?";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("删除游戏存档失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 按存档编号读取存档。
     *
     * @param username 用户名
     * @param saveId 存档编号
     * @return 游戏存档记录，如果不存在则为空
     */
    public Optional<GameSaveRecord> findGameSave(String username, long saveId) {
        String sql = "SELECT * FROM game_saves WHERE username = ? AND save_id = ?";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setLong(2, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(toRecord(connection, resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("读取游戏失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 读取指定用户最近更新的存档。
     *
     * @param username 用户名
     * @return 游戏存档记录，如果不存在则为空
     */
    public Optional<GameSaveRecord> findLatestGameSave(String username) {
        String sql = "SELECT * FROM game_saves WHERE username = ? ORDER BY updated_at DESC LIMIT 1";
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(toRecord(connection, resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("读取游戏失败：" + exception.getMessage(), exception);
        }
    }

    /**
     * 查询玩家所有存档摘要。
     *
     * @param username 用户名
     * @return 存档摘要列表
     */
    public List<GameSaveSummary> listGameSaves(String username) {
        String sql = """
                SELECT save_id, save_name, current_room_id, current_level, money, stamina, status, updated_at
                FROM game_saves WHERE username = ? ORDER BY updated_at DESC
                """;
        List<GameSaveSummary> summaries = new ArrayList<>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    summaries.add(new GameSaveSummary(resultSet.getLong("save_id"),
                            resultSet.getString("save_name"), resultSet.getString("current_room_id"),
                            resultSet.getInt("current_level"), resultSet.getInt("money"),
                            resultSet.getInt("stamina"), resultSet.getString("status"),
                            resultSet.getString("updated_at")));
                }
            }
            return summaries;
        } catch (SQLException exception) {
            throw new IllegalStateException("查询存档列表失败：" + exception.getMessage(), exception);
        }
    }

    private long saveMainRecord(Connection connection, GameSaveRecord record) throws SQLException {
        if (record.getSaveId() == null) {
            return insertMainRecord(connection, record);
        }
        updateMainRecord(connection, record);
        return record.getSaveId();
    }

    private long insertMainRecord(Connection connection, GameSaveRecord record) throws SQLException {
        String sql = """
                INSERT INTO game_saves(username, save_name, session_id, current_room_id, previous_room_id,
                money, stamina, max_stamina, max_weight, current_level, level_start_room_id,
                level_start_money, level_start_max_weight, level_start_inventory_items, final_score,
                status, password_unlocked, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillMainRecord(statement, record);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
            throw new IllegalStateException("保存游戏失败：未生成存档编号");
        }
    }

    private void updateMainRecord(Connection connection, GameSaveRecord record) throws SQLException {
        String sql = """
                UPDATE game_saves SET save_name = ?, session_id = ?, current_room_id = ?, previous_room_id = ?,
                money = ?, stamina = ?, max_stamina = ?, max_weight = ?, current_level = ?,
                level_start_room_id = ?, level_start_money = ?, level_start_max_weight = ?,
                level_start_inventory_items = ?, final_score = ?, status = ?, password_unlocked = ?,
                updated_at = ? WHERE username = ? AND save_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, record.getSaveName());
            statement.setString(2, record.getSessionId());
            statement.setString(3, record.getCurrentRoomId());
            statement.setString(4, record.getPreviousRoomId());
            statement.setInt(5, record.getMoney());
            statement.setInt(6, record.getStamina());
            statement.setInt(7, record.getMaxStamina());
            statement.setInt(8, record.getMaxWeight());
            statement.setInt(9, record.getCurrentLevel());
            statement.setString(10, record.getLevelStartRoomId());
            statement.setInt(11, record.getLevelStartMoney());
            statement.setInt(12, record.getLevelStartMaxWeight());
            statement.setString(13, record.getLevelStartInventoryItems());
            statement.setInt(14, record.getFinalScore());
            statement.setString(15, record.getStatus().name());
            statement.setInt(16, record.isPasswordUnlocked() ? 1 : 0);
            statement.setString(17, Instant.now().toString());
            statement.setString(18, record.getUsername());
            statement.setLong(19, record.getSaveId());
            statement.executeUpdate();
        }
    }

    private void fillMainRecord(PreparedStatement statement, GameSaveRecord record) throws SQLException {
        statement.setString(1, record.getUsername());
        statement.setString(2, record.getSaveName());
        statement.setString(3, record.getSessionId());
        statement.setString(4, record.getCurrentRoomId());
        statement.setString(5, record.getPreviousRoomId());
        statement.setInt(6, record.getMoney());
        statement.setInt(7, record.getStamina());
        statement.setInt(8, record.getMaxStamina());
        statement.setInt(9, record.getMaxWeight());
        statement.setInt(10, record.getCurrentLevel());
        statement.setString(11, record.getLevelStartRoomId());
        statement.setInt(12, record.getLevelStartMoney());
        statement.setInt(13, record.getLevelStartMaxWeight());
        statement.setString(14, record.getLevelStartInventoryItems());
        statement.setInt(15, record.getFinalScore());
        statement.setString(16, record.getStatus().name());
        statement.setInt(17, record.isPasswordUnlocked() ? 1 : 0);
        statement.setString(18, Instant.now().toString());
    }

    private void replaceInventoryItems(Connection connection, long saveId, String inventoryItems) throws SQLException {
        deleteChildren(connection, "save_inventory_items", saveId);
        String sql = "INSERT INTO save_inventory_items(save_id, item_order, item_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 0;
            for (String itemId : splitList(inventoryItems)) {
                statement.setLong(1, saveId);
                statement.setInt(2, index++);
                statement.setString(3, itemId);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void replaceRoomItems(Connection connection, long saveId, String roomItems) throws SQLException {
        deleteChildren(connection, "save_room_items", saveId);
        String sql = "INSERT INTO save_room_items(save_id, room_id, item_order, item_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String roomPart : roomItems.split("\\|")) {
                String[] roomAndItems = roomPart.split(":", 2);
                if (roomAndItems.length < 2 || roomAndItems[1].isBlank()) {
                    continue;
                }
                int index = 0;
                for (String itemId : splitList(roomAndItems[1])) {
                    statement.setLong(1, saveId);
                    statement.setString(2, roomAndItems[0]);
                    statement.setInt(3, index++);
                    statement.setString(4, itemId);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    private void replaceLogs(Connection connection, long saveId, String logs) throws SQLException {
        deleteChildren(connection, "save_logs", saveId);
        String sql = "INSERT INTO save_logs(save_id, log_order, message) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 0;
            for (String log : splitLogs(logs)) {
                statement.setLong(1, saveId);
                statement.setInt(2, index++);
                statement.setString(3, log);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void deleteChildren(Connection connection, String tableName, long saveId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE save_id = ?")) {
            statement.setLong(1, saveId);
            statement.executeUpdate();
        }
    }

    private GameSaveRecord toRecord(Connection connection, ResultSet resultSet) throws SQLException {
        long saveId = resultSet.getLong("save_id");
        return new GameSaveRecord(
                resultSet.getString("username"),
                saveId,
                resultSet.getString("save_name"),
                resultSet.getString("session_id"),
                resultSet.getString("current_room_id"),
                resultSet.getString("previous_room_id"),
                resultSet.getInt("money"),
                resultSet.getInt("stamina"),
                resultSet.getInt("max_stamina"),
                resultSet.getInt("max_weight"),
                resultSet.getInt("current_level"),
                resultSet.getString("level_start_room_id"),
                resultSet.getInt("level_start_money"),
                resultSet.getInt("level_start_max_weight"),
                resultSet.getString("level_start_inventory_items"),
                resultSet.getInt("final_score"),
                GameStatus.valueOf(resultSet.getString("status")),
                resultSet.getInt("password_unlocked") == 1,
                loadInventoryItems(connection, saveId),
                loadRoomItems(connection, saveId),
                loadLogs(connection, saveId));
    }

    private String loadInventoryItems(Connection connection, long saveId) throws SQLException {
        String sql = "SELECT item_id FROM save_inventory_items WHERE save_id = ? ORDER BY item_order";
        List<String> itemIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    itemIds.add(resultSet.getString("item_id"));
                }
            }
        }
        return String.join(",", itemIds);
    }

    private String loadRoomItems(Connection connection, long saveId) throws SQLException {
        String sql = """
                SELECT room_id, item_id FROM save_room_items
                WHERE save_id = ? ORDER BY room_id, item_order
                """;
        List<String> roomParts = new ArrayList<>();
        String currentRoomId = null;
        List<String> currentItems = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String roomId = resultSet.getString("room_id");
                    if (currentRoomId != null && !currentRoomId.equals(roomId)) {
                        roomParts.add(currentRoomId + ":" + String.join(",", currentItems));
                        currentItems.clear();
                    }
                    currentRoomId = roomId;
                    currentItems.add(resultSet.getString("item_id"));
                }
            }
        }
        if (currentRoomId != null) {
            roomParts.add(currentRoomId + ":" + String.join(",", currentItems));
        }
        return String.join("|", roomParts);
    }

    private String loadLogs(Connection connection, long saveId) throws SQLException {
        String sql = "SELECT message FROM save_logs WHERE save_id = ? ORDER BY log_order";
        List<String> logs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, saveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(resultSet.getString("message"));
                }
            }
        }
        return String.join("\n", logs);
    }

    private void initializeDatabase() {
        try {
            Path parent = databasePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("创建数据库目录失败：" + exception.getMessage(), exception);
        }
        try (Connection connection = openConnection()) {
            createUsersTable(connection);
            migrateOldGameSavesIfNeeded(connection);
            createSaveTables(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("初始化 SQLite 数据库失败：" + exception.getMessage(), exception);
        }
    }

    private void createUsersTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        username TEXT PRIMARY KEY,
                        password_hash TEXT NOT NULL,
                        high_score INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL
                    )
                    """);
        }
        addColumnIfMissing(connection, "users", "high_score", "INTEGER NOT NULL DEFAULT 0");
    }

    private void createSaveTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS game_saves (
                        save_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL,
                        save_name TEXT NOT NULL,
                        session_id TEXT NOT NULL,
                        current_room_id TEXT NOT NULL,
                        previous_room_id TEXT,
                        money INTEGER NOT NULL,
                        stamina INTEGER NOT NULL,
                        max_stamina INTEGER NOT NULL,
                        max_weight INTEGER NOT NULL,
                        current_level INTEGER NOT NULL,
                        level_start_room_id TEXT NOT NULL,
                        level_start_money INTEGER NOT NULL,
                        level_start_max_weight INTEGER NOT NULL,
                        level_start_inventory_items TEXT NOT NULL,
                        final_score INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        password_unlocked INTEGER NOT NULL,
                        updated_at TEXT NOT NULL,
                        FOREIGN KEY(username) REFERENCES users(username)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS save_inventory_items (
                        save_id INTEGER NOT NULL,
                        item_order INTEGER NOT NULL,
                        item_id TEXT NOT NULL,
                        FOREIGN KEY(save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS save_room_items (
                        save_id INTEGER NOT NULL,
                        room_id TEXT NOT NULL,
                        item_order INTEGER NOT NULL,
                        item_id TEXT NOT NULL,
                        FOREIGN KEY(save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
                    )
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS save_logs (
                        save_id INTEGER NOT NULL,
                        log_order INTEGER NOT NULL,
                        message TEXT NOT NULL,
                        FOREIGN KEY(save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
                    )
                    """);
        }
    }

    private void migrateOldGameSavesIfNeeded(Connection connection) throws SQLException {
        if (!tableExists(connection, "game_saves") || columnExists(connection, "game_saves", "save_id")) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE game_saves RENAME TO game_saves_legacy");
        }
        createSaveTables(connection);
        String sql = """
                SELECT username, session_id, current_room_id, previous_room_id, money, stamina, max_stamina,
                max_weight, current_level, level_start_room_id, level_start_money, level_start_max_weight,
                level_start_inventory_items, final_score, status, password_unlocked, inventory_items,
                room_items, logs FROM game_saves_legacy
                """;
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                GameSaveRecord record = new GameSaveRecord(resultSet.getString("username"), null, "迁移存档",
                        resultSet.getString("session_id"), resultSet.getString("current_room_id"),
                        resultSet.getString("previous_room_id"), resultSet.getInt("money"),
                        resultSet.getInt("stamina"), resultSet.getInt("max_stamina"),
                        resultSet.getInt("max_weight"), resultSet.getInt("current_level"),
                        resultSet.getString("level_start_room_id"), resultSet.getInt("level_start_money"),
                        resultSet.getInt("level_start_max_weight"),
                        resultSet.getString("level_start_inventory_items"), resultSet.getInt("final_score"),
                        GameStatus.valueOf(resultSet.getString("status")),
                        resultSet.getInt("password_unlocked") == 1, resultSet.getString("inventory_items"),
                        resultSet.getString("room_items"), resultSet.getString("logs"));
                long saveId = insertMainRecord(connection, record);
                replaceInventoryItems(connection, saveId, record.getInventoryItems());
                replaceRoomItems(connection, saveId, record.getRoomItems());
                replaceLogs(connection, saveId, record.getLogs());
            }
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE game_saves_legacy");
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                if (columnName.equals(resultSet.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addColumnIfMissing(Connection connection, String tableName, String columnName, String definition)
            throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private List<String> splitList(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String item : value.split(",")) {
            if (!item.isBlank()) {
                result.add(item);
            }
        }
        return result;
    }

    private List<String> splitLogs(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String log : value.split("\\n")) {
            if (!log.isBlank()) {
                result.add(log);
            }
        }
        return result;
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }
}

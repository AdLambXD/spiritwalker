package com.adlamb.simplexiuzhen.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;

import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 玩家数据访问对象
 * 处理玩家数据的数据库操作
 */
public class PlayerDataDAO {
    private final SimpleXiuzhen plugin;
    private final DatabaseManager databaseManager;

    public PlayerDataDAO(SimpleXiuzhen plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /**
     * 保存玩家数据到数据库
     */
    public void savePlayerData(UUID playerUUID, String currentRealm, int subLevelIndex, 
                              int currentExp, boolean isMeditating, Location lastLocation) {
        // 只有在使用数据库时才执行
        if (!databaseManager.getStorageType().isDatabase()) {
            return;
        }

        String sql = """
            INSERT INTO player_data (uuid, current_realm, current_sub_level, current_exp, 
                                   is_meditating, last_location_x, last_location_y, last_location_z, 
                                   last_location_world, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE
                current_realm = VALUES(current_realm),
                current_sub_level = VALUES(current_sub_level),
                current_exp = VALUES(current_exp),
                is_meditating = VALUES(is_meditating),
                last_location_x = VALUES(last_location_x),
                last_location_y = VALUES(last_location_y),
                last_location_z = VALUES(last_location_z),
                last_location_world = VALUES(last_location_world),
                updated_at = CURRENT_TIMESTAMP
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerUUID.toString());
            statement.setString(2, currentRealm);
            statement.setInt(3, subLevelIndex);
            statement.setInt(4, currentExp);
            statement.setBoolean(5, isMeditating);
            
            if (lastLocation != null) {
                statement.setDouble(6, lastLocation.getX());
                statement.setDouble(7, lastLocation.getY());
                statement.setDouble(8, lastLocation.getZ());
                statement.setString(9, lastLocation.getWorld().getName());
            } else {
                statement.setNull(6, Types.DOUBLE);
                statement.setNull(7, Types.DOUBLE);
                statement.setNull(8, Types.DOUBLE);
                statement.setNull(9, Types.VARCHAR);
            }
            
            statement.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家数据失败: " + playerUUID, e);
        }
    }

    /**
     * 从数据库加载玩家数据
     */
    public PlayerDataResult loadPlayerData(UUID playerUUID) {
        String sql = "SELECT * FROM player_data WHERE uuid = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, playerUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return new PlayerDataResult(
                    resultSet.getString("current_realm"),
                    resultSet.getInt("current_sub_level"),
                    resultSet.getInt("current_exp"),
                    resultSet.getBoolean("is_meditating"),
                    createLocation(
                        resultSet.getDouble("last_location_x"),
                        resultSet.getDouble("last_location_y"),
                        resultSet.getDouble("last_location_z"),
                        resultSet.getString("last_location_world")
                    )
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "加载玩家数据失败: " + playerUUID, e);
        }
        
        // 返回默认值
        return new PlayerDataResult("LianQi", 0, 0, false, null);
    }

    /**
     * 创建位置对象
     */
    private Location createLocation(double x, double y, double z, String worldName) {
        if (worldName == null) return null;
        
        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        
        return new Location(world, x, y, z);
    }

    /**
     * 玩家数据结果类
     */
    public static class PlayerDataResult {
        private final String currentRealm;
        private final int subLevelIndex;
        private final int currentExp;
        private final boolean isMeditating;
        private final Location lastLocation;

        public PlayerDataResult(String currentRealm, int subLevelIndex, int currentExp, 
                               boolean isMeditating, Location lastLocation) {
            this.currentRealm = currentRealm;
            this.subLevelIndex = subLevelIndex;
            this.currentExp = currentExp;
            this.isMeditating = isMeditating;
            this.lastLocation = lastLocation;
        }

        // Getter方法
        public String getCurrentRealm() { return currentRealm; }
        public int getSubLevelIndex() { return subLevelIndex; }
        public int getCurrentExp() { return currentExp; }
        public boolean isMeditating() { return isMeditating; }
        public Location getLastLocation() { return lastLocation; }
    }
}
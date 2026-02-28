package com.adlamb.simplexiuzhen.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.constants.XiuzhenConstants;
import com.adlamb.simplexiuzhen.enums.StorageType;
import com.adlamb.simplexiuzhen.utils.XiuzhenUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 数据库管理器
 * 支持 MySQL 存储方式
 */
public class DatabaseManager {
    private final SimpleXiuzhen plugin;
    private HikariDataSource dataSource;
    private StorageType storageType;

    public DatabaseManager(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.storageType = StorageType.fromString(
            plugin.getConfig().getString("settings.data.storage_type", XiuzhenConstants.DEFAULT_STORAGE_TYPE)
        );
        initializeDatabase();
    }

    /**
     * 初始化数据库连接
     */
    private void initializeDatabase() {
        try {
            if (storageType.isDatabase()) {
                setupMySQL();
                createTables();
                plugin.getLogger().info("MySQL数据库连接初始化成功");
            } else {
                // YAML模式，不需要初始化数据库连接
                plugin.getLogger().info("使用YAML存储模式，跳过数据库初始化");
                return;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "数据库初始化失败! 切换到YAML模式", e);
            this.storageType = StorageType.YAML; // 失败时回退到YAML模式
        }
    }

    /**
     * 设置 MySQL 连接
     */
    private void setupMySQL() {
        FileConfiguration config = plugin.getConfig();
        HikariConfig hikariConfig = new HikariConfig();
        
        String host = XiuzhenUtils.getConfigString(config, "settings.data.mysql.host", XiuzhenConstants.DEFAULT_MYSQL_HOST);
        int port = XiuzhenUtils.getConfigInt(config, "settings.data.mysql.port", XiuzhenConstants.DEFAULT_MYSQL_PORT);
        String database = XiuzhenUtils.getConfigString(config, "settings.data.mysql.database", XiuzhenConstants.DEFAULT_MYSQL_DATABASE);
        String username = XiuzhenUtils.getConfigString(config, "settings.data.mysql.username", XiuzhenConstants.DEFAULT_MYSQL_USERNAME);
        String password = XiuzhenUtils.getConfigString(config, "settings.data.mysql.password", "");
        
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + 
                              "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        
        // 连接池配置
        hikariConfig.setMaximumPoolSize(XiuzhenUtils.getConfigInt(config, "settings.data.mysql.maximum_pool_size", 
            XiuzhenConstants.DEFAULT_MYSQL_MAX_POOL_SIZE));
        hikariConfig.setMinimumIdle(XiuzhenUtils.getConfigInt(config, "settings.data.mysql.minimum_idle", 
            XiuzhenConstants.DEFAULT_MYSQL_MIN_IDLE));
        hikariConfig.setConnectionTimeout(XiuzhenUtils.getConfigLong(config, "settings.data.mysql.connection_timeout", 
            XiuzhenConstants.DEFAULT_MYSQL_CONNECTION_TIMEOUT));
        hikariConfig.setIdleTimeout(XiuzhenUtils.getConfigLong(config, "settings.data.mysql.idle_timeout", 
            XiuzhenConstants.DEFAULT_MYSQL_IDLE_TIMEOUT));
        hikariConfig.setMaxLifetime(XiuzhenUtils.getConfigLong(config, "settings.data.mysql.max_lifetime", 
            XiuzhenConstants.DEFAULT_MYSQL_MAX_LIFETIME));
        
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * 创建数据表
     */
    private void createTables() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            
            // 玩家基础数据表
            String playerDataTable = """
                CREATE TABLE IF NOT EXISTS player_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    current_realm VARCHAR(50) NOT NULL DEFAULT 'LianQi',
                    current_sub_level INTEGER NOT NULL DEFAULT 0,
                    current_exp INTEGER NOT NULL DEFAULT 0,
                    is_meditating BOOLEAN NOT NULL DEFAULT FALSE,
                    last_location_x DOUBLE,
                    last_location_y DOUBLE,
                    last_location_z DOUBLE,
                    last_location_world VARCHAR(100),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            // 玩家属性表
            String playerAttributesTable = """
                CREATE TABLE IF NOT EXISTS player_attributes (
                    uuid VARCHAR(36) PRIMARY KEY,
                    max_health DOUBLE NOT NULL DEFAULT 20.0,
                    max_mana INTEGER NOT NULL DEFAULT 100,
                    move_speed DOUBLE NOT NULL DEFAULT 0.2,
                    damage_reduction DOUBLE NOT NULL DEFAULT 0.0,
                    FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
                )
                """;
            
            // 玩家功法表
            String playerSkillsTable = """
                CREATE TABLE IF NOT EXISTS player_skills (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    uuid VARCHAR(36) NOT NULL,
                    skill_name VARCHAR(100) NOT NULL,
                    skill_level INTEGER NOT NULL DEFAULT 1,
                    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
                )
                """;
            
            // 玩家物品表
            String playerItemsTable = """
                CREATE TABLE IF NOT EXISTS player_items (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    uuid VARCHAR(36) NOT NULL,
                    item_type VARCHAR(50) NOT NULL,
                    item_data TEXT, -- JSON格式存储物品数据
                    quantity INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (uuid) REFERENCES player_data(uuid) ON DELETE CASCADE
                )
                """;
            
            statement.execute(playerDataTable);
            statement.execute(playerAttributesTable);
            statement.execute(playerSkillsTable);
            statement.execute(playerItemsTable);
            
            plugin.getLogger().info("MySQL数据表创建完成");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "创建数据表失败!", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        if (!storageType.isDatabase()) {
            throw new SQLException("当前使用YAML存储模式，无数据库连接");
        }
        if (dataSource == null) {
            throw new SQLException("数据库连接池未初始化");
        }
        return dataSource.getConnection();
    }

    /**
     * 关闭数据库连接池
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("数据库连接池已关闭");
        }
    }

    /**
     * 获取存储类型
     */
    public StorageType getStorageType() {
        return storageType;
    }

    /**
     * 获取存储类型字符串
     */
    public String getStorageTypeString() {
        return storageType.getTypeName();
    }

    /**
     * 重新加载数据库配置
     */
    public void reload() {
        close();
        this.storageType = StorageType.fromString(
            plugin.getConfig().getString("settings.data.storage_type", XiuzhenConstants.DEFAULT_STORAGE_TYPE)
        );
        initializeDatabase();
    }
}
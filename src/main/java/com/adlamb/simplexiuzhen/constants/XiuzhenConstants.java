package com.adlamb.simplexiuzhen.constants;

/**
 * 常量类 - 集中管理所有常量值
 */
public class XiuzhenConstants {
    
    // 插件基本信息
    public static final String PLUGIN_NAME = "SimpleXiuzhen";
    public static final String PLUGIN_VERSION = "1.0-SNAPSHOT";
    public static final String AUTHOR = "adlamb";
    
    // 默认配置值
    public static final double DEFAULT_BASE_GAIN_PER_SECOND = 0.05;
    public static final double DEFAULT_MOVE_DISTANCE_THRESHOLD = 0.5;
    public static final double DEFAULT_SEAT_MULTIPLIER = 1.5;
    public static final int DEFAULT_DETECTION_INTERVAL = 10;
    public static final int DEFAULT_STAND_COOLDOWN = 40;
    public static final int DEFAULT_AUTO_SAVE_INTERVAL = 5;
    
    // 数据库相关常量
    public static final String DEFAULT_STORAGE_TYPE = "YAML";
    public static final String DEFAULT_MYSQL_HOST = "localhost";
    public static final int DEFAULT_MYSQL_PORT = 3306;
    public static final String DEFAULT_MYSQL_DATABASE = "simplexiuzhen";
    public static final String DEFAULT_MYSQL_USERNAME = "root";
    public static final int DEFAULT_MYSQL_MAX_POOL_SIZE = 10;
    public static final int DEFAULT_MYSQL_MIN_IDLE = 2;
    public static final long DEFAULT_MYSQL_CONNECTION_TIMEOUT = 30000L;
    public static final long DEFAULT_MYSQL_IDLE_TIMEOUT = 600000L;
    public static final long DEFAULT_MYSQL_MAX_LIFETIME = 1800000L;
    
    // 分页相关常量
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;
    public static final int MIN_PAGE_SIZE = 1;
    
    // 时间相关常量（毫秒）
    public static final long SECOND_IN_MILLIS = 1000L;
    public static final long MINUTE_IN_MILLIS = 60000L;
    public static final long HOUR_IN_MILLIS = 3600000L;
    public static final long DAY_IN_MILLIS = 86400000L;
    
    // 配置文件路径
    public static final String CONFIG_FILE = "config.yml";
    public static final String REALMS_FILE = "realms.yml";
    public static final String DATA_FOLDER = "player_data";
    public static final String BACKUP_FOLDER = "backups";
    
    // 权限节点
    public static final String PERMISSION_USE = "simplexiuzhen.use";
    public static final String PERMISSION_ADMIN = "simplexiuzhen.admin";
    
    // 命令别名
    public static final String[] XIUZHEN_ALIASES = {"xz", "xiuxian"};
    public static final String[] XIUZHEN_ADMIN_ALIASES = {"xza", "xzadmin"};
    
    // 第三方插件名称
    public static final String PLUGIN_CMI = "CMI";
    public static final String PLUGIN_ESSENTIALS = "Essentials";
    public static final String PLUGIN_SIT = "Sit";
    
    // 方块类型（可坐下的方块）
    public static final String[] SITTABLE_BLOCK_SUFFIXES = {
        "STAIRS", "SLAB", "CARPET", "LILY_PAD", "SNOW"
    };
    
    private XiuzhenConstants() {
        // 私有构造函数防止实例化
    }
}
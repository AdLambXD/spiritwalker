package com.adlamb.simplexiuzhen;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.adlamb.simplexiuzhen.constants.XiuzhenConstants;

/**
 * 配置管理器类，负责加载和管理配置文件
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration realmsConfig;
    private FileConfiguration martialRealmsConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    /**
     * 加载所有配置文件
     */
    private void loadConfigs() {
        // 加载主配置文件
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // 加载境界配置文件
        loadRealmsConfig();
        
        // 加载武者境界配置文件
        loadMartialRealmsConfig();
    }

    /**
     * 加载境界配置文件
     */
    private void loadRealmsConfig() {
        File realmsFile = new File(plugin.getDataFolder(), "realms.yml");
        
        // 如果文件不存在，从资源中复制默认配置
        if (!realmsFile.exists()) {
            plugin.saveResource("realms.yml", false);
        }
        
        realmsConfig = YamlConfiguration.loadConfiguration(realmsFile);
    }
    
    /**
     * 加载武者境界配置文件
     */
    private void loadMartialRealmsConfig() {
        File martialRealmsFile = new File(plugin.getDataFolder(), "martial_realms.yml");
        
        // 如果文件不存在，从资源中复制默认配置
        if (!martialRealmsFile.exists()) {
            plugin.saveResource("martial_realms.yml", false);
        }
        
        martialRealmsConfig = YamlConfiguration.loadConfiguration(martialRealmsFile);
    }

    /**
     * 获取基础修为增长值（每秒）
     */
    public double getBaseGainPerSecond() {
        return config.getDouble("settings.cultivation.base_gain_per_second", XiuzhenConstants.DEFAULT_BASE_GAIN_PER_SECOND);
    }

    /**
     * 获取移动距离阈值
     */
    public double getMoveDistanceThreshold() {
        return config.getDouble("settings.cultivation.move_distance_threshold", 0.5);
    }

    /**
     * 获取自动保存间隔（分钟）
     */
    public int getAutoSaveIntervalMinutes() {
        return config.getInt("settings.data.auto_save_interval_minutes", 5);
    }

    /**
     * 获取境界配置
     */
    public FileConfiguration getRealmsConfig() {
        return realmsConfig;
    }
    
    /**
     * 获取武者境界配置
     */
    public FileConfiguration getMartialRealmsConfig() {
        return martialRealmsConfig;
    }

    /**
     * 重新加载所有配置
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadRealmsConfig();
        loadMartialRealmsConfig();
    }
}
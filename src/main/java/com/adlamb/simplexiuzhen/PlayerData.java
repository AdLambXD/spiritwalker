package com.adlamb.simplexiuzhen;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * 玩家数据类，管理单个玩家的修仙信息
 */
public class PlayerData {
    private final UUID playerUUID;
    private String currentRealmKey; // 当前境界Key
    private int currentSubLevelIndex; // 当前段位索引
    private double currentExp; // 当前段位修为（浮点数）
    private boolean isMeditating; // 是否在打坐
    private Location lastLocation; // 上次位置（用于检测移动）

    private final SimpleXiuzhen plugin;
    private final ConfigManager configManager;

    public PlayerData(UUID playerUUID, SimpleXiuzhen plugin) {
        this.playerUUID = playerUUID;
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.currentRealmKey = "LianQi"; // 默认境界
        this.currentSubLevelIndex = 0; // 默认段位
        this.currentExp = 0;
        this.isMeditating = false;
    }

    /**
     * 从文件加载玩家数据
     */
    public void loadData() {
        File dataFolder = new File(plugin.getDataFolder(), "player_data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dataFile = new File(dataFolder, playerUUID.toString() + ".yml");
        if (!dataFile.exists()) {
            // 文件不存在，使用默认值
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            this.currentRealmKey = config.getString("current_realm", "LianQi");
            this.currentSubLevelIndex = config.getInt("current_sub_level_index", 0);
            this.currentExp = config.getInt("current_exp", 0);
            this.isMeditating = config.getBoolean("is_meditating", false);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "加载玩家数据失败: " + playerUUID, e);
        }
    }

    /**
     * 保存玩家数据到文件
     */
    public void saveData() {
        File dataFolder = new File(plugin.getDataFolder(), "player_data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dataFile = new File(dataFolder, playerUUID.toString() + ".yml");

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            config.set("current_realm", this.currentRealmKey);
            config.set("current_sub_level_index", this.currentSubLevelIndex);
            config.set("current_exp", this.currentExp);
            config.set("is_meditating", this.isMeditating);
            
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家数据失败: " + playerUUID, e);
        }
    }

    /**
     * 增加修为
     */
    public void addExp(double amount) {
        if (amount <= 0) return;

        this.currentExp += amount;
        
        // 检查是否需要升级段位
        checkLevelUp();
    }

    /**
     * 检查并处理等级提升
     */
    private void checkLevelUp() {
        FileConfiguration realmsConfig = configManager.getRealmsConfig();
        
        // 获取当前境界的所有段位
        String realmPath = "realms." + this.currentRealmKey + ".sub_levels";
        if (!realmsConfig.contains(realmPath)) {
            return;
        }
        
        // 使用更安全的方式获取段位列表
        try {
            java.util.List<java.util.Map<?, ?>> subLevels = realmsConfig.getMapList(realmPath);
            if (subLevels == null || subLevels.isEmpty()) {
                return;
            }
            
            // 检查是否有下一个段位可以升级到
            int nextSubLevelIndex = this.currentSubLevelIndex + 1;
            
            if (nextSubLevelIndex < subLevels.size()) {
                // 获取下一段位的要求
                java.util.Map<?, ?> nextSubLevel = subLevels.get(nextSubLevelIndex);
                Object expObj = nextSubLevel.get("required_exp");
                int requiredExp = 0;
                if (expObj instanceof Number) {
                    requiredExp = ((Number) expObj).intValue();
                }
                
                // 如果当前修为达到了下一段位的要求，则升级
                if (this.currentExp >= requiredExp) {
                    // 升级到下一段位
                    this.currentSubLevelIndex = nextSubLevelIndex;
                    
                    plugin.getLogger().info("玩家 " + playerUUID + " 从 " + this.currentRealmKey + 
                        " 第" + (nextSubLevelIndex) + "层 升级到第" + (nextSubLevelIndex + 1) + "层");
                    
                    // 继续检查是否还能继续升级（递归检查）
                    checkLevelUp();
                }
            } else {
                // 当前已是该境界的最高段位，检查是否可以进入下一境界
                advanceToNextRealm();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("检查等级升级时出错: " + e.getMessage());
        }
    }

    /**
     * 进入下一境界
     */
    private void advanceToNextRealm() {
        // 简化的境界晋升逻辑
        // 实际应用中应该根据境界配置进行更精确的判断
        if ("LianQi".equals(this.currentRealmKey)) {
            this.currentRealmKey = "ZhuJi";
        } else if ("ZhuJi".equals(this.currentRealmKey)) {
            this.currentRealmKey = "JinDan";
        } else if ("JinDan".equals(this.currentRealmKey)) {
            this.currentRealmKey = "YuanYing";
        }
        
        // 重置段位和修为
        this.currentSubLevelIndex = 0;
        this.currentExp = 0;
        
        plugin.getLogger().info("玩家 " + playerUUID + " 晋升到境界: " + this.currentRealmKey);
    }

    /**
     * 切换打坐状态
     */
    public void toggleMeditation(Player player) {
        this.isMeditating = !this.isMeditating;
        if (this.isMeditating) {
            this.lastLocation = player.getLocation().clone();
        }
    }

    /**
     * 检查玩家是否移动了（用于打坐检测）
     */
    public boolean hasMoved(Location currentLocation) {
        if (lastLocation == null) return false;
        
        double distance = lastLocation.distance(currentLocation);
        return distance > configManager.getMoveDistanceThreshold();
    }

    /**
     * 更新最后位置
     */
    public void updateLastLocation(Location location) {
        this.lastLocation = location.clone();
    }

    // Getter和Setter方法
    public String getCurrentRealmKey() { return currentRealmKey; }
    public void setCurrentRealmKey(String currentRealmKey) { this.currentRealmKey = currentRealmKey; }
    
    public int getCurrentSubLevelIndex() { return currentSubLevelIndex; }
    public void setCurrentSubLevelIndex(int currentSubLevelIndex) { this.currentSubLevelIndex = currentSubLevelIndex; }
    
    public double getCurrentExp() { return currentExp; }
    public void setCurrentExp(double currentExp) { this.currentExp = currentExp; }
    
    public boolean isMeditating() { return isMeditating; }
    public void setMeditating(boolean meditating) { isMeditating = meditating; }
    
    public Location getLastLocation() { return lastLocation; }
}
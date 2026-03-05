package com.adlamb.simplexiuzhen;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * 玩家数据类，管理单个玩家的修仙和武者双系统信息
 */
public class PlayerData {
    private final UUID playerUUID;
    private final SimpleXiuzhen plugin;
    private final ConfigManager configManager;
    
    // ========== 修仙系统数据 ==========
    private String currentRealmKey; // 当前境界 Key
    private int currentSubLevelIndex; // 当前段位索引
    private double currentExp; // 当前段位修为（浮点数）
    private double currentLingli; // 当前灵力值
    private double maxLingli; // 最大灵力值
    
    // ========== 武者系统数据 ==========
    private String currentWushuRealmKey; // 当前武者境界 Key
    private int currentWushuSubLevelIndex; // 当前武者段位索引
    private double currentWushuExp; // 当前武者修为
    private double currentNeili; // 当前内力值
    private double maxNeili; // 最大内力值
    
    // ========== 状态数据 ==========
    private boolean isMeditating; // 是否在打坐
    private boolean isInCombatTraining; // 是否在武者训练
    private Location lastLocation; // 上次位置（用于检测移动）
    private long lastCombatTime; // 上次战斗时间戳

    public PlayerData(UUID playerUUID, SimpleXiuzhen plugin) {
        this.playerUUID = playerUUID;
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        
        // 修仙系统默认值
        this.currentRealmKey = "LianQi";
        this.currentSubLevelIndex = 0;
        this.currentExp = 0;
        this.currentLingli = 100;
        this.maxLingli = 100;
        
        // 武者系统默认值
        this.currentWushuRealmKey = "XiuLian";
        this.currentWushuSubLevelIndex = 0;
        this.currentWushuExp = 0;
        this.currentNeili = 100;
        this.maxNeili = 100;
        
        // 状态默认值
        this.isMeditating = false;
        this.isInCombatTraining = false;
        this.lastCombatTime = 0;
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
            calculateMaxResources();
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            
            // 加载修仙系统数据
            this.currentRealmKey = config.getString("xiuzhen.current_realm", "LianQi");
            this.currentSubLevelIndex = config.getInt("xiuzhen.current_sub_level_index", 0);
            this.currentExp = config.getDouble("xiuzhen.current_exp", 0);
            this.currentLingli = config.getDouble("xiuzhen.current_lingli", 100);
            this.maxLingli = config.getDouble("xiuzhen.max_lingli", 100);
            
            // 加载武者系统数据
            this.currentWushuRealmKey = config.getString("wushu.current_realm", "XiuLian");
            this.currentWushuSubLevelIndex = config.getInt("wushu.current_sub_level_index", 0);
            this.currentWushuExp = config.getDouble("wushu.current_exp", 0);
            this.currentNeili = config.getDouble("wushu.current_neili", 100);
            this.maxNeili = config.getDouble("wushu.max_neili", 100);
            
            // 加载状态数据
            this.isMeditating = config.getBoolean("status.is_meditating", false);
            this.isInCombatTraining = config.getBoolean("status.is_combat_training", false);
            this.lastCombatTime = config.getLong("status.last_combat_time", 0);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "加载玩家数据失败：" + playerUUID, e);
        }
        
        // 计算最大资源
        calculateMaxResources();
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
            
            // 保存修仙系统数据
            config.set("xiuzhen.current_realm", this.currentRealmKey);
            config.set("xiuzhen.current_sub_level_index", this.currentSubLevelIndex);
            config.set("xiuzhen.current_exp", this.currentExp);
            config.set("xiuzhen.current_lingli", this.currentLingli);
            config.set("xiuzhen.max_lingli", this.maxLingli);
            
            // 保存武者系统数据
            config.set("wushu.current_realm", this.currentWushuRealmKey);
            config.set("wushu.current_sub_level_index", this.currentWushuSubLevelIndex);
            config.set("wushu.current_exp", this.currentWushuExp);
            config.set("wushu.current_neili", this.currentNeili);
            config.set("wushu.max_neili", this.maxNeili);
            
            // 保存状态数据
            config.set("status.is_meditating", this.isMeditating);
            config.set("status.is_combat_training", this.isInCombatTraining);
            config.set("status.last_combat_time", this.lastCombatTime);
            
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家数据失败：" + playerUUID, e);
        }
    }

    /**
     * 计算最大灵力和内力值
     */
    private void calculateMaxResources() {
        // 计算最大灵力（基于修仙境界）
        FileConfiguration realmsConfig = configManager.getRealmsConfig();
        int xiuzhenLevelBonus = 0;
        
        try {
            String path = "realms." + this.currentRealmKey + ".level";
            xiuzhenLevelBonus = realmsConfig.getInt(path, 1) * 100;
        } catch (Exception e) {
            xiuzhenLevelBonus = 100;
        }
        
        this.maxLingli = 100 + xiuzhenLevelBonus + (this.currentSubLevelIndex * 20);
        
        // 确保当前灵力不超过最大值
        if (this.currentLingli > this.maxLingli) {
            this.currentLingli = this.maxLingli;
        }
        
        // 计算最大内力（基于武者境界）
        FileConfiguration martialConfig = configManager.getMartialRealmsConfig();
        int wushuLevelBonus = 0;
        
        try {
            String path = "martial_realms." + this.currentWushuRealmKey + ".level";
            wushuLevelBonus = martialConfig.getInt(path, 1) * 100;
        } catch (Exception e) {
            wushuLevelBonus = 100;
        }
        
        this.maxNeili = 100 + wushuLevelBonus + (this.currentWushuSubLevelIndex * 20);
        
        // 确保当前内力不超过最大值
        if (this.currentNeili > this.maxNeili) {
            this.currentNeili = this.maxNeili;
        }
    }

    /**
     * 增加修仙修为
     */
    public void addExp(double amount) {
        if (amount <= 0) return;

        this.currentExp += amount;
        checkXiuzhenLevelUp();
    }
    
    /**
     * 增加武者修为
     */
    public void addWushuExp(double amount) {
        if (amount <= 0) return;
        
        this.currentWushuExp += amount;
        checkWushuLevelUp();
    }

    /**
     * 检查修仙境界升级
     */
    private void checkXiuzhenLevelUp() {
        FileConfiguration realmsConfig = configManager.getRealmsConfig();
        String realmPath = "realms." + this.currentRealmKey + ".sub_levels";
        
        if (!realmsConfig.contains(realmPath)) return;
        
        try {
            List<?> subLevels = realmsConfig.getList(realmPath);
            if (subLevels == null || subLevels.isEmpty()) return;
            
            int nextSubLevelIndex = this.currentSubLevelIndex + 1;
            
            if (nextSubLevelIndex < subLevels.size()) {
                Object subLevelObj = subLevels.get(nextSubLevelIndex);
                if (subLevelObj instanceof java.util.Map) {
                    java.util.Map<?, ?> subLevel = (java.util.Map<?, ?>) subLevelObj;
                    Object expObj = subLevel.get("required_exp");
                    int requiredExp = 0;
                    if (expObj instanceof Number) {
                        requiredExp = ((Number) expObj).intValue();
                    }
                    
                    if (this.currentExp >= requiredExp) {
                        this.currentSubLevelIndex = nextSubLevelIndex;
                        plugin.getLogger().info("玩家 " + playerUUID + " 修仙境界升级到 " + 
                            this.currentRealmKey + " 第" + (nextSubLevelIndex + 1) + "层");
                        calculateMaxResources();
                        checkXiuzhenLevelUp();
                    }
                }
            } else {
                advanceToNextRealm();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("检查修仙境界升级时出错：" + e.getMessage());
        }
    }
    
    /**
     * 检查武者境界升级
     */
    private void checkWushuLevelUp() {
        FileConfiguration martialConfig = configManager.getMartialRealmsConfig();
        String realmPath = "martial_realms." + this.currentWushuRealmKey + ".sub_levels";
        
        if (!martialConfig.contains(realmPath)) return;
        
        try {
            List<?> subLevels = martialConfig.getList(realmPath);
            if (subLevels == null || subLevels.isEmpty()) return;
            
            int nextSubLevelIndex = this.currentWushuSubLevelIndex + 1;
            
            if (nextSubLevelIndex < subLevels.size()) {
                Object subLevelObj = subLevels.get(nextSubLevelIndex);
                if (subLevelObj instanceof java.util.Map) {
                    java.util.Map<?, ?> subLevel = (java.util.Map<?, ?>) subLevelObj;
                    Object expObj = subLevel.get("required_exp");
                    int requiredExp = 0;
                    if (expObj instanceof Number) {
                        requiredExp = ((Number) expObj).intValue();
                    }
                    
                    if (this.currentWushuExp >= requiredExp) {
                        this.currentWushuSubLevelIndex = nextSubLevelIndex;
                        plugin.getLogger().info("玩家 " + playerUUID + " 武者境界升级到 " + 
                            this.currentWushuRealmKey + " 第" + (nextSubLevelIndex + 1) + "层");
                        calculateMaxResources();
                        checkWushuLevelUp();
                    }
                }
            } else {
                advanceToNextWushuRealm();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("检查武者境界升级时出错：" + e.getMessage());
        }
    }

    /**
     * 进入下一修仙境界
     */
    private void advanceToNextRealm() {
        if ("LianQi".equals(this.currentRealmKey)) {
            this.currentRealmKey = "ZhuJi";
        } else if ("ZhuJi".equals(this.currentRealmKey)) {
            this.currentRealmKey = "JinDan";
        } else if ("JinDan".equals(this.currentRealmKey)) {
            this.currentRealmKey = "YuanYing";
        }
        
        this.currentSubLevelIndex = 0;
        this.currentExp = 0;
        calculateMaxResources();
        plugin.getLogger().info("玩家 " + playerUUID + " 晋升到修仙境界：" + this.currentRealmKey);
    }
    
    /**
     * 进入下一武者境界
     */
    private void advanceToNextWushuRealm() {
        if ("XiuLian".equals(this.currentWushuRealmKey)) {
            this.currentWushuRealmKey = "TongMai";
        } else if ("TongMai".equals(this.currentWushuRealmKey)) {
            this.currentWushuRealmKey = "ZhuJing";
        }
        
        this.currentWushuSubLevelIndex = 0;
        this.currentWushuExp = 0;
        calculateMaxResources();
        plugin.getLogger().info("玩家 " + playerUUID + " 晋升到武者境界：" + this.currentWushuRealmKey);
    }

    /**
     * 消耗灵力
     */
    public boolean consumeLingli(double amount) {
        if (this.currentLingli >= amount) {
            this.currentLingli -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * 消耗内力
     */
    public boolean consumeNeili(double amount) {
        if (this.currentNeili >= amount) {
            this.currentNeili -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * 恢复灵力
     */
    public void recoverLingli(double amount) {
        this.currentLingli = Math.min(this.currentLingli + amount, this.maxLingli);
    }
    
    /**
     * 恢复内力
     */
    public void recoverNeili(double amount) {
        this.currentNeili = Math.min(this.currentNeili + amount, this.maxNeili);
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
     * 切换武者训练状态
     */
    public void toggleCombatTraining(Player player) {
        this.isInCombatTraining = !this.isInCombatTraining;
        if (this.isInCombatTraining) {
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
    
    /**
     * 记录战斗时间
     */
    public void recordCombatTime() {
        this.lastCombatTime = System.currentTimeMillis();
    }
    
    /**
     * 检查是否处于战斗状态（5 秒内有战斗）
     */
    public boolean isInCombat() {
        return (System.currentTimeMillis() - this.lastCombatTime) < 5000;
    }

    // ========== Getter 和 Setter 方法 ==========
    
    // 修仙系统 Getter
    public String getCurrentRealmKey() { return currentRealmKey; }
    public void setCurrentRealmKey(String currentRealmKey) { 
        this.currentRealmKey = currentRealmKey; 
        calculateMaxResources();
    }
    
    public int getCurrentSubLevelIndex() { return currentSubLevelIndex; }
    public void setCurrentSubLevelIndex(int currentSubLevelIndex) { 
        this.currentSubLevelIndex = currentSubLevelIndex; 
        calculateMaxResources();
    }
    
    public double getCurrentExp() { return currentExp; }
    public void setCurrentExp(double currentExp) { this.currentExp = currentExp; }
    
    public double getCurrentLingli() { return currentLingli; }
    public void setCurrentLingli(double currentLingli) { this.currentLingli = currentLingli; }
    
    public double getMaxLingli() { return maxLingli; }
    
    // 武者系统 Getter
    public String getCurrentWushuRealmKey() { return currentWushuRealmKey; }
    public void setCurrentWushuRealmKey(String currentWushuRealmKey) { 
        this.currentWushuRealmKey = currentWushuRealmKey; 
        calculateMaxResources();
    }
    
    public int getCurrentWushuSubLevelIndex() { return currentWushuSubLevelIndex; }
    public void setCurrentWushuSubLevelIndex(int currentWushuSubLevelIndex) { 
        this.currentWushuSubLevelIndex = currentWushuSubLevelIndex; 
        calculateMaxResources();
    }
    
    public double getCurrentWushuExp() { return currentWushuExp; }
    public void setCurrentWushuExp(double currentWushuExp) { this.currentWushuExp = currentWushuExp; }
    
    public double getCurrentNeili() { return currentNeili; }
    public void setCurrentNeili(double currentNeili) { this.currentNeili = currentNeili; }
    
    public double getMaxNeili() { return maxNeili; }
    
    // 状态 Getter
    public boolean isMeditating() { return isMeditating; }
    public void setMeditating(boolean meditating) { isMeditating = meditating; }
    
    public boolean isInCombatTraining() { return isInCombatTraining; }
    public void setInCombatTraining(boolean inCombatTraining) { isInCombatTraining = inCombatTraining; }
    
    public Location getLastLocation() { return lastLocation; }
}

package com.adlamb.simplexiuzhen.permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 权限管理器
 * 管理修仙系统的权限控制
 */
public class XiuzhenPermissions {
    private final SimpleXiuzhen plugin;
    private final FileConfiguration config;
    private final Map<String, String> realmPermissions;

    public XiuzhenPermissions(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.realmPermissions = new HashMap<>();
        loadPermissions();
    }

    /**
     * 加载权限配置
     */
    private void loadPermissions() {
        // 从配置文件加载境界权限
        FileConfiguration realmsConfig = plugin.getConfigManager().getRealmsConfig();
        Set<String> realms = realmsConfig.getConfigurationSection("realms").getKeys(false);
        
        for (String realmKey : realms) {
            String permission = realmsConfig.getString("realms." + realmKey + ".permission");
            if (permission != null && !permission.isEmpty()) {
                realmPermissions.put(realmKey, permission);
            }
        }
        
        plugin.getLogger().info("加载了 " + realmPermissions.size() + " 个境界权限");
    }

    /**
     * 检查玩家是否有指定境界的权限
     */
    public boolean hasRealmPermission(Player player, String realmKey) {
        String requiredPermission = realmPermissions.get(realmKey);
        if (requiredPermission == null) {
            // 如果没有配置权限，默认允许
            return true;
        }
        
        return player.hasPermission(requiredPermission);
    }

    /**
     * 检查玩家是否有修炼权限
     */
    public boolean hasCultivationPermission(Player player) {
        return player.hasPermission("simplexiuzhen.cultivate") || 
               player.hasPermission("simplexiuzhen.use");
    }

    /**
     * 检查玩家是否有查看排行榜权限
     */
    public boolean hasRankingPermission(Player player) {
        return player.hasPermission("simplexiuzhen.ranking") || 
               player.hasPermission("simplexiuzhen.use");
    }

    /**
     * 检查玩家是否有坐下修炼权限
     */
    public boolean hasSittingPermission(Player player) {
        return player.hasPermission("simplexiuzhen.sit") || 
               player.hasPermission("simplexiuzhen.cultivate");
    }

    /**
     * 获取玩家最高可达到的境界
     */
    public String getHighestAccessibleRealm(Player player) {
        FileConfiguration realmsConfig = plugin.getConfigManager().getRealmsConfig();
        Set<String> realms = realmsConfig.getConfigurationSection("realms").getKeys(false);
        
        String highestRealm = "LianQi"; // 默认最低境界
        
        for (String realmKey : realms) {
            if (hasRealmPermission(player, realmKey)) {
                highestRealm = realmKey;
            }
        }
        
        return highestRealm;
    }

    /**
     * 重新加载权限配置
     */
    public void reloadPermissions() {
        realmPermissions.clear();
        loadPermissions();
    }

    /**
     * 获取所有境界权限映射
     */
    public Map<String, String> getRealmPermissions() {
        return new HashMap<>(realmPermissions);
    }
}
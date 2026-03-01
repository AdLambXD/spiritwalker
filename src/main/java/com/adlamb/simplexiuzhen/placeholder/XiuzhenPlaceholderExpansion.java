package com.adlamb.simplexiuzhen.placeholder;

import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * PlaceholderAPI 扩展
 * 为修仙插件提供占位符支持
 */
public class XiuzhenPlaceholderExpansion extends PlaceholderExpansion {
    private final SimpleXiuzhen plugin;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public XiuzhenPlaceholderExpansion(SimpleXiuzhen plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "simplexiuzhen";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }

        UUID playerId = player.getUniqueId();
        PlayerData playerData = plugin.getPlayerData(playerId);

        // 基础信息占位符
        switch (identifier.toLowerCase()) {
            case "realm":
                return playerData.getCurrentRealmKey();
                
            case "realm_display":
                return plugin.getConfigManager().getRealmsConfig()
                    .getString("realms." + playerData.getCurrentRealmKey() + ".display_name", "未知境界");
                
            case "sublevel_index":
                return String.valueOf(playerData.getCurrentSubLevelIndex());
                
            case "sublevel_name": {
                try {
                    String realmKey = playerData.getCurrentRealmKey();
                    int subLevelIndex = playerData.getCurrentSubLevelIndex();
                    java.util.List<?> subLevels = plugin.getConfigManager().getRealmsConfig()
                        .getMapList("realms." + realmKey + ".sub_levels");
                    
                    if (subLevelIndex >= 0 && subLevelIndex < subLevels.size()) {
                        Object subLevelObj = subLevels.get(subLevelIndex);
                        if (subLevelObj instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> subLevel = (java.util.Map<String, Object>) subLevelObj;
                            return (String) subLevel.get("name");
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("获取段位名称失败: " + e.getMessage());
                }
                return "未知段位";
            }
            
            case "exp":
                return decimalFormat.format(playerData.getCurrentExp());
                
            case "exp_int":
                return String.valueOf((int) playerData.getCurrentExp());
                
            case "is_meditating":
                return String.valueOf(playerData.isMeditating());
                
            case "meditation_status":
                return playerData.isMeditating() ? "打坐中" : "未打坐";
                
            case "formatted_exp":
                return formatExp(playerData.getCurrentExp());
                
            case "next_level_exp": {
                try {
                    String realmKey = playerData.getCurrentRealmKey();
                    int subLevelIndex = playerData.getCurrentSubLevelIndex();
                    java.util.List<?> subLevels = plugin.getConfigManager().getRealmsConfig()
                        .getMapList("realms." + realmKey + ".sub_levels");
                    
                    int nextSubLevelIndex = subLevelIndex + 1;
                    if (nextSubLevelIndex < subLevels.size()) {
                        Object subLevelObj = subLevels.get(nextSubLevelIndex);
                        if (subLevelObj instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> subLevel = (java.util.Map<String, Object>) subLevelObj;
                            Object expObj = subLevel.get("required_exp");
                            if (expObj instanceof Number) {
                                return String.valueOf(((Number) expObj).intValue());
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("获取下级修为要求失败: " + e.getMessage());
                }
                return "已满级";
            }
            
            case "progress_percentage": {
                try {
                    String realmKey = playerData.getCurrentRealmKey();
                    int subLevelIndex = playerData.getCurrentSubLevelIndex();
                    java.util.List<?> subLevels = plugin.getConfigManager().getRealmsConfig()
                        .getMapList("realms." + realmKey + ".sub_levels");
                    
                    int nextSubLevelIndex = subLevelIndex + 1;
                    if (nextSubLevelIndex < subLevels.size()) {
                        Object subLevelObj = subLevels.get(nextSubLevelIndex);
                        if (subLevelObj instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> subLevel = (java.util.Map<String, Object>) subLevelObj;
                            Object expObj = subLevel.get("required_exp");
                            if (expObj instanceof Number) {
                                double requiredExp = ((Number) expObj).doubleValue();
                                if (requiredExp > 0) {
                                    double progress = (playerData.getCurrentExp() / requiredExp) * 100;
                                    return decimalFormat.format(Math.min(progress, 100));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("计算进度百分比失败: " + e.getMessage());
                }
                return "100";
            }
            
            case "health_bonus": {
                int subLevelIndex = playerData.getCurrentSubLevelIndex();
                double healthBonus = subLevelIndex * 2.0;
                return decimalFormat.format(healthBonus);
            }
            
            case "mana_bonus": {
                int subLevelIndex = playerData.getCurrentSubLevelIndex();
                int manaBonus = subLevelIndex * 5;
                return String.valueOf(manaBonus);
            }
            
            case "total_attributes": {
                int subLevelIndex = playerData.getCurrentSubLevelIndex();
                double healthBonus = subLevelIndex * 2.0;
                int manaBonus = subLevelIndex * 5;
                return "生命+" + decimalFormat.format(healthBonus) + ", 灵力+" + manaBonus;
            }
            
            case "rank_position": {
                // 这里可以实现排行榜位置查询
                // 需要访问排行榜管理器
                return "#999";
            }
            
            case "formatted_realm":
                return getFormattedRealm(playerData);
                
            case "status_icon":
                return playerData.isMeditating() ? "🧘" : "❌";
                
            case "cultivation_speed": {
                double baseSpeed = plugin.getConfig().getDouble("settings.cultivation.base_gain_per_second", 0.05);
                if (player instanceof Player && player.isOnline()) {
                    Player onlinePlayer = (Player) player;
                    // 可以根据是否在盔甲架上增加速度
                    if (plugin.getRideMeditationListener().isPlayerRiding(playerId)) {
                        baseSpeed *= plugin.getConfig().getDouble("settings.cultivation.armor_stand_meditation.multiplier", 2.0);
                    }
                }
                return decimalFormat.format(baseSpeed);
            }
        }

        return null; // 返回null表示该占位符未被处理
    }

    /**
     * 格式化修为显示
     */
    private String formatExp(double exp) {
        if (exp >= 1000000) {
            return decimalFormat.format(exp / 1000000) + "M";
        } else if (exp >= 1000) {
            return decimalFormat.format(exp / 1000) + "K";
        } else {
            return decimalFormat.format(exp);
        }
    }

    /**
     * 获取格式化的境界显示
     */
    private String getFormattedRealm(PlayerData playerData) {
        try {
            String realmKey = playerData.getCurrentRealmKey();
            String realmDisplayName = plugin.getConfigManager().getRealmsConfig()
                .getString("realms." + realmKey + ".display_name", "未知境界");
            
            int subLevelIndex = playerData.getCurrentSubLevelIndex();
            java.util.List<?> subLevels = plugin.getConfigManager().getRealmsConfig()
                .getMapList("realms." + realmKey + ".sub_levels");
            
            if (subLevelIndex >= 0 && subLevelIndex < subLevels.size()) {
                Object subLevelObj = subLevels.get(subLevelIndex);
                if (subLevelObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> subLevel = (java.util.Map<String, Object>) subLevelObj;
                    String subLevelName = (String) subLevel.get("name");
                    return realmDisplayName + subLevelName;
                }
            }
            return realmDisplayName;
        } catch (Exception e) {
            plugin.getLogger().warning("获取格式化境界失败: " + e.getMessage());
            return "未知境界";
        }
    }
}
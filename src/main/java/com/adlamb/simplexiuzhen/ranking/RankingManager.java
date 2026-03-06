package com.adlamb.simplexiuzhen.ranking;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * 排行榜管理器
 * 负责处理修仙排行榜相关功能
 */
public class RankingManager {
    private final SimpleXiuzhen plugin;
    private final FileConfiguration realmsConfig;

    public RankingManager(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.realmsConfig = plugin.getConfigManager().getRealmsConfig();
    }

    /**
     * 获取修仙排行榜数据
     */
    public List<PlayerRanking> getRankings() {
        List<PlayerRanking> rankings = new ArrayList<>();
        
        // 获取所有在线玩家的数据
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.hasPlayedBefore()) {
                try {
                    PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
                    if (playerData != null) {
                        String realmDisplayName = getRealmDisplayName(playerData.getCurrentRealmKey());
                        String subLevelName = getSubLevelName(playerData.getCurrentRealmKey(), playerData.getCurrentSubLevelIndex());
                        String fullRealmName = realmDisplayName + subLevelName;
                        int totalExp = calculateTotalExp(playerData);
                        
                        rankings.add(new PlayerRanking(
                            player.getName(),
                            fullRealmName,
                            totalExp,
                            playerData.getCurrentExp()
                        ));
                    }
                } catch (Exception e) {
                    // 忽略无法加载数据的玩家
                }
            }
        }
        
        // 按总修为排序
        rankings.sort((a, b) -> Double.compare(b.getTotalExp(), a.getTotalExp()));
        return rankings;
    }

    /**
     * 显示排行榜
     */
    public void displayRankings(CommandSender sender, int page) {
        List<PlayerRanking> rankings = getRankings();
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) rankings.size() / pageSize);
        
        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;
        
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, rankings.size());
        
        sender.sendMessage(NamedTextColor.GOLD + "=== 修仙排行榜 (第" + page + "/" + totalPages + "页) ===");
        sender.sendMessage(Component.text("排名  玩家名        境界        总修为    当前修为", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("--------------------------------------------------", NamedTextColor.GRAY));
        
        if (rankings.isEmpty()) {
            sender.sendMessage(Component.text("暂无玩家数据", NamedTextColor.YELLOW));
            return;
        }
        
        for (int i = startIndex; i < endIndex; i++) {
            PlayerRanking ranking = rankings.get(i);
            int displayRank = i + 1;
            
            String rankColorStr = displayRank <= 3 ? 
                (displayRank == 1 ? NamedTextColor.GOLD.toString() : 
                 displayRank == 2 ? NamedTextColor.AQUA.toString() : NamedTextColor.LIGHT_PURPLE.toString()) : 
                NamedTextColor.WHITE.toString();
            
            sender.sendMessage(rankColorStr + String.format("%-4d", displayRank) + " " +
                NamedTextColor.WHITE.toString() + String.format("%-12s", ranking.getPlayerName()) + " " +
                NamedTextColor.AQUA.toString() + String.format("%-10s", ranking.getRealmName()) + " " +
                NamedTextColor.GREEN.toString() + String.format("%-8d", ranking.getTotalExp()) + " " +
                NamedTextColor.YELLOW.toString() + ranking.getCurrentExp());
        }
        
        sender.sendMessage("");
        if (totalPages > 1) {
            sender.sendMessage(Component.text("使用 /xiuzhen top <页码> 查看其他页面", NamedTextColor.GRAY));
        }
    }

    /**
     * 获取境界显示名称
     */
    private String getRealmDisplayName(String realmKey) {
        return realmsConfig.getString("realms." + realmKey + ".display_name", realmKey);
    }

    /**
     * 获取段位名称
     */
    private String getSubLevelName(String realmKey, int subLevelIndex) {
        String path = "realms." + realmKey + ".sub_levels." + subLevelIndex + ".name";
        return realmsConfig.getString(path, "");
    }

    /**
     * 计算总修为（当前境界段位要求 + 当前修为）
     */
    private int calculateTotalExp(PlayerData playerData) {
        int baseExp = 0;
        
        // 计算当前段位之前的修为要求
        String realmKey = playerData.getCurrentRealmKey();
        for (int i = 0; i < playerData.getCurrentSubLevelIndex(); i++) {
            String path = "realms." + realmKey + ".sub_levels." + i + ".required_exp";
            baseExp += getConfigInt(path, 0);
        }
        
        // 加上当前修为
        return (int) (baseExp + playerData.getCurrentExp());
    }

    /**
     * 安全获取配置中的整数值
     */
    private int getConfigInt(String path, int defaultValue) {
        Object value = realmsConfig.get(path);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else {
            return defaultValue;
        }
    }

    /**
     * 玩家排行榜数据类
     */
    public static class PlayerRanking {
        private final String playerName;
        private final String realmName;
        private final double totalExp;
        private final double currentExp;

        public PlayerRanking(String playerName, String realmName, double totalExp, double currentExp) {
            this.playerName = playerName;
            this.realmName = realmName;
            this.totalExp = totalExp;
            this.currentExp = currentExp;
        }

        // Getter方法
        public String getPlayerName() { return playerName; }
        public String getRealmName() { return realmName; }
        public double getTotalExp() { return totalExp; }
        public double getCurrentExp() { return currentExp; }
    }
}
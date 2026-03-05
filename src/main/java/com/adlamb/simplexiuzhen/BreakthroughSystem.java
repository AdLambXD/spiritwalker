package com.adlamb.simplexiuzhen;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 境界突破系统
 * 处理修仙和武者的境界突破逻辑
 */
public class BreakthroughSystem {
    private final SimpleXiuzhen plugin;
    private final ConfigManager configManager;
    
    public BreakthroughSystem(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }
    
    /**
     * 检查并尝试突破修仙境界
     */
    public boolean attemptXiuzhenBreakthrough(Player player, PlayerData playerData) {
        FileConfiguration realmsConfig = configManager.getRealmsConfig();
        String currentRealm = playerData.getCurrentRealmKey();
        int currentSubLevel = playerData.getCurrentSubLevelIndex();
        
        // 获取当前境界的所有段位
        String realmPath = "realms." + currentRealm + ".sub_levels";
        if (!realmsConfig.contains(realmPath)) {
            player.sendMessage(ChatColor.RED + "当前境界配置异常！");
            return false;
        }
        
        try {
            List<?> subLevels = realmsConfig.getList(realmPath);
            if (subLevels == null || subLevels.isEmpty()) {
                player.sendMessage(ChatColor.RED + "境界段位配置为空！");
                return false;
            }
            
            // 检查是否已经是最高段位
            if (currentSubLevel >= subLevels.size() - 1) {
                // 需要突破到下一境界
                return attemptNextXiuzhenRealmBreakthrough(player, playerData, currentRealm);
            } else {
                // 突破到下一段位
                return attemptSubLevelBreakthrough(player, playerData, "xiuzhen", 
                    currentRealm, currentSubLevel + 1, subLevels);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("检查修仙境界突破时出错: " + e.getMessage());
            player.sendMessage(ChatColor.RED + "突破检查出现错误！");
            return false;
        }
    }
    
    /**
     * 检查并尝试突破武者境界
     */
    public boolean attemptWushuBreakthrough(Player player, PlayerData playerData) {
        FileConfiguration martialConfig = configManager.getMartialRealmsConfig();
        String currentRealm = playerData.getCurrentWushuRealmKey();
        int currentSubLevel = playerData.getCurrentWushuSubLevelIndex();
        
        // 获取当前境界的所有段位
        String realmPath = "martial_realms." + currentRealm + ".sub_levels";
        if (!martialConfig.contains(realmPath)) {
            player.sendMessage(ChatColor.RED + "当前武者境界配置异常！");
            return false;
        }
        
        try {
            List<?> subLevels = martialConfig.getList(realmPath);
            if (subLevels == null || subLevels.isEmpty()) {
                player.sendMessage(ChatColor.RED + "武者境界段位配置为空！");
                return false;
            }
            
            // 检查是否已经是最高段位
            if (currentSubLevel >= subLevels.size() - 1) {
                // 需要突破到下一境界
                return attemptNextWushuRealmBreakthrough(player, playerData, currentRealm);
            } else {
                // 突破到下一段位
                return attemptSubLevelBreakthrough(player, playerData, "wushu", 
                    currentRealm, currentSubLevel + 1, subLevels);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("检查武者境界突破时出错: " + e.getMessage());
            player.sendMessage(ChatColor.RED + "突破检查出现错误！");
            return false;
        }
    }
    
    /**
     * 突破到下一段位
     */
    private boolean attemptSubLevelBreakthrough(Player player, PlayerData playerData, 
            String systemType, String realmKey, int targetSubLevel, List<?> subLevels) {
        
        try {
            Object subLevelObj = subLevels.get(targetSubLevel);
            if (!(subLevelObj instanceof Map)) {
                player.sendMessage(ChatColor.RED + "段位配置格式错误！");
                return false;
            }
            
            Map<?, ?> subLevel = (Map<?, ?>) subLevelObj;
            Object expObj = subLevel.get("required_exp");
            Object neiliObj = subLevel.get("required_neili"); // 武者系统特有
            
            int requiredExp = 0;
            int requiredNeili = 0;
            
            if (expObj instanceof Number) {
                requiredExp = ((Number) expObj).intValue();
            }
            
            if (neiliObj instanceof Number) {
                requiredNeili = ((Number) neiliObj).intValue();
            }
            
            // 检查修为要求
            double currentExp = "xiuzhen".equals(systemType) ? 
                playerData.getCurrentExp() : playerData.getCurrentWushuExp();
                
            if (currentExp < requiredExp) {
                player.sendMessage(ChatColor.RED + "修为不足！需要 " + requiredExp + " 点修为。");
                return false;
            }
            
            // 检查内力要求（仅武者系统）
            if ("wushu".equals(systemType) && playerData.getCurrentNeili() < requiredNeili) {
                player.sendMessage(ChatColor.RED + "内力不足！需要 " + requiredNeili + " 点内力。");
                return false;
            }
            
            // 检查物品要求
            if (!checkBreakthroughItems(player, systemType, realmKey)) {
                return false;
            }
            
            // 消耗物品
            consumeBreakthroughItems(player, systemType, realmKey);
            
            // 执行突破
            if ("xiuzhen".equals(systemType)) {
                playerData.setCurrentSubLevelIndex(targetSubLevel);
                player.sendMessage(ChatColor.GREEN + "恭喜你成功突破到新的修仙段位！");
            } else {
                playerData.setCurrentWushuSubLevelIndex(targetSubLevel);
                player.sendMessage(ChatColor.GREEN + "恭喜你成功突破到新的武者段位！");
            }
            
            plugin.getLogger().info("玩家 " + player.getName() + " 在 " + systemType + " 系统中突破到 " + 
                realmKey + " 第" + (targetSubLevel + 1) + "层");
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("执行段位突破时出错: " + e.getMessage());
            player.sendMessage(ChatColor.RED + "突破过程中出现错误！");
            return false;
        }
    }
    
    /**
     * 突破到下一修仙境界
     */
    private boolean attemptNextXiuzhenRealmBreakthrough(Player player, PlayerData playerData, String currentRealm) {
        // 简化实现，实际应该读取配置文件中的突破要求
        String nextRealm = getNextXiuzhenRealm(currentRealm);
        
        if (nextRealm == null) {
            player.sendMessage(ChatColor.RED + "已达到最高等境界！");
            return false;
        }
        
        // 检查突破物品
        if (!checkBreakthroughItems(player, "xiuzhen", nextRealm)) {
            return false;
        }
        
        // 消耗物品
        consumeBreakthroughItems(player, "xiuzhen", nextRealm);
        
        // 执行境界突破
        playerData.setCurrentRealmKey(nextRealm);
        playerData.setCurrentSubLevelIndex(0);
        playerData.setCurrentExp(0);
        
        player.sendMessage(ChatColor.GOLD + "天道有感！恭喜你成功突破到 " + nextRealm + " 境界！");
        plugin.getLogger().info("玩家 " + player.getName() + " 突破到修仙境界: " + nextRealm);
        
        return true;
    }
    
    /**
     * 突破到下一武者境界
     */
    private boolean attemptNextWushuRealmBreakthrough(Player player, PlayerData playerData, String currentRealm) {
        String nextRealm = getNextWushuRealm(currentRealm);
        
        if (nextRealm == null) {
            player.sendMessage(ChatColor.RED + "已达到最高武者境界！");
            return false;
        }
        
        // 检查突破物品
        if (!checkBreakthroughItems(player, "wushu", nextRealm)) {
            return false;
        }
        
        // 消耗物品
        consumeBreakthroughItems(player, "wushu", nextRealm);
        
        // 执行境界突破
        playerData.setCurrentWushuRealmKey(nextRealm);
        playerData.setCurrentWushuSubLevelIndex(0);
        playerData.setCurrentWushuExp(0);
        
        player.sendMessage(ChatColor.GOLD + "武道通神！恭喜你成功突破到 " + nextRealm + " 境界！");
        plugin.getLogger().info("玩家 " + player.getName() + " 突破到武者境界: " + nextRealm);
        
        return true;
    }
    
    /**
     * 检查突破所需物品
     */
    private boolean checkBreakthroughItems(Player player, String systemType, String realmKey) {
        FileConfiguration config = "xiuzhen".equals(systemType) ? 
            configManager.getRealmsConfig() : configManager.getMartialRealmsConfig();
            
        String itemsPath = ("xiuzhen".equals(systemType) ? "realms." : "martial_realms.") + 
            realmKey + ".breakthrough_items";
            
        if (!config.contains(itemsPath)) {
            return true; // 没有物品要求
        }
        
        try {
            List<?> items = config.getList(itemsPath);
            if (items == null || items.isEmpty()) {
                return true;
            }
            
            for (Object itemObj : items) {
                if (itemObj instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) itemObj;
                    String itemMaterialStr = (String) itemMap.get("item");
                    int amount = ((Number) itemMap.get("amount")).intValue();
                    String itemName = (String) itemMap.get("name");
                    
                    Material material = Material.getMaterial(itemMaterialStr);
                    if (material == null) continue;
                    
                    if (!hasItemCount(player, material, amount)) {
                        player.sendMessage(ChatColor.RED + "缺少突破物品: " + itemName + " x" + amount);
                        return false;
                    }
                }
            }
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("检查突破物品时出错: " + e.getMessage());
            return true; // 出错时允许突破
        }
    }
    
    /**
     * 消耗突破物品
     */
    private void consumeBreakthroughItems(Player player, String systemType, String realmKey) {
        FileConfiguration config = "xiuzhen".equals(systemType) ? 
            configManager.getRealmsConfig() : configManager.getMartialRealmsConfig();
            
        String itemsPath = ("xiuzhen".equals(systemType) ? "realms." : "martial_realms.") + 
            realmKey + ".breakthrough_items";
            
        if (!config.contains(itemsPath)) {
            return;
        }
        
        try {
            List<?> items = config.getList(itemsPath);
            if (items == null || items.isEmpty()) {
                return;
            }
            
            for (Object itemObj : items) {
                if (itemObj instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) itemObj;
                    String itemMaterialStr = (String) itemMap.get("item");
                    int amount = ((Number) itemMap.get("amount")).intValue();
                    
                    Material material = Material.getMaterial(itemMaterialStr);
                    if (material == null) continue;
                    
                    removeItemCount(player, material, amount);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("消耗突破物品时出错: " + e.getMessage());
        }
    }
    
    /**
     * 检查玩家是否拥有指定数量的物品
     */
    private boolean hasItemCount(Player player, Material material, int requiredAmount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count >= requiredAmount;
    }
    
    /**
     * 从玩家背包中移除指定数量的物品
     */
    private void removeItemCount(Player player, Material material, int amountToRemove) {
        int remaining = amountToRemove;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && remaining > 0) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    item.setAmount(0);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }
    }
    
    /**
     * 获取下一修仙境界
     */
    private String getNextXiuzhenRealm(String currentRealm) {
        // 简化实现，实际应该从配置读取
        switch (currentRealm) {
            case "LianQi": return "ZhuJi";
            case "ZhuJi": return "JinDan";
            case "JinDan": return "YuanYing";
            case "YuanYing": return "HuaShen";
            default: return null;
        }
    }
    
    /**
     * 获取下一武者境界
     */
    private String getNextWushuRealm(String currentRealm) {
        // 简化实现，实际应该从配置读取
        switch (currentRealm) {
            case "XiuLian": return "TongMai";
            case "TongMai": return "ZhuJing";
            case "ZhuJing": return "HuaYuan";
            default: return null;
        }
    }
}
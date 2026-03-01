package com.adlamb.simplexiuzhen.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.adlamb.simplexiuzhen.EnhancedKungFu;
import com.adlamb.simplexiuzhen.EnhancedKungFuManager;
import com.adlamb.simplexiuzhen.EnhancedPlayerData;
import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.lang.LanguageManager;

/**
 * GUI管理器
 * 负责创建和管理各种修仙系统界面
 */
public class GuiManager {
    private final SimpleXiuzhen plugin;
    private final LanguageManager languageManager;
    private final EnhancedKungFuManager kungFuManager;
    
    // 存储打开GUI的玩家，防止重复打开
    private final Map<UUID, String> openGuis;
    
    public GuiManager(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.kungFuManager = plugin.getEnhancedKungFuManager();
        this.openGuis = new HashMap<>();
    }
    
    /**
     * 打开主修仙界面
     */
    public void openMainGui(Player player) {
        if (isOpen(player)) {
            closeGui(player);
        }
        
        Inventory gui = Bukkit.createInventory(null, 27, "§6修仙系统");
        
        // 填充背景
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }
        
        // 玩家状态面板
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        ItemStack statusPanel = createPlayerStatusItem(playerData);
        gui.setItem(13, statusPanel);
        
        // 功能按钮
        gui.setItem(10, createMeditateButtonItem(playerData));
        gui.setItem(11, createStatsButtonItem());
        gui.setItem(12, createRankingsButtonItem());
        gui.setItem(14, createKungFuButtonItem());
        gui.setItem(15, createRealmInfoButtonItem());
        gui.setItem(16, createCloseButtonItem());
        
        // 装饰性边框
        ItemStack border = createItem(Material.CYAN_STAINED_GLASS_PANE, "§b修仙之道", null);
        gui.setItem(0, border);
        gui.setItem(8, border);
        gui.setItem(18, border);
        gui.setItem(26, border);
        
        player.openInventory(gui);
        setOpen(player, "main");
    }
    
    /**
     * 打开功法界面
     */
    public void openKungFuGui(Player player) {
        if (isOpen(player)) {
            closeGui(player);
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "§d功法系统");
        
        // 填充背景
        ItemStack background = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, background);
        }
        
        // 获取玩家可学习的功法
        EnhancedPlayerData playerData = plugin.getEnhancedPlayerData(player.getUniqueId());
        List<EnhancedKungFu> availableKungFus = kungFuManager.getAvailableKungFus(playerData);
        
        // 显示功法（每页最多45个）
        int slot = 0;
        for (EnhancedKungFu kungFu : availableKungFus) {
            if (slot >= 45) break; // 只显示第一页
            
            ItemStack kungFuItem = createEnhancedKungFuItem(kungFu, playerData);
            gui.setItem(slot, kungFuItem);
            slot++;
        }
        
        // 返回按钮
        gui.setItem(49, createBackButtonItem("main"));
        
        player.openInventory(gui);
        setOpen(player, "kungfu");
    }
    
    /**
     * 打开境界信息界面
     */
    public void openRealmInfoGui(Player player) {
        if (isOpen(player)) {
            closeGui(player);
        }
        
        Inventory gui = Bukkit.createInventory(null, 27, "§e境界体系");
        
        // 填充背景
        ItemStack background = createItem(Material.ORANGE_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, background);
        }
        
        // 显示各个境界信息
        String[] realms = {"LianQi", "ZhuJi", "JinDan", "YuanYing"};
        String[] realmNames = {"炼气", "筑基", "金丹", "元婴"};
        Material[] realmMaterials = {Material.COAL, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND};
        
        for (int i = 0; i < Math.min(realms.length, 4); i++) {
            ItemStack realmItem = createRealmInfoItem(realms[i], realmNames[i], realmMaterials[i]);
            gui.setItem(10 + i * 2, realmItem);
        }
        
        // 返回按钮
        gui.setItem(22, createBackButtonItem("main"));
        
        player.openInventory(gui);
        setOpen(player, "realm_info");
    }
    
    /**
     * 创建玩家状态物品
     */
    private ItemStack createPlayerStatusItem(PlayerData playerData) {
        Material material = Material.PLAYER_HEAD;
        String displayName = "§a" + playerData.getCurrentRealmKey() + "境界";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7境界: §f" + playerData.getCurrentRealmKey());
        lore.add("§7修为: §f" + String.format("%.2f", playerData.getCurrentExp()));
        lore.add("§7状态: " + (playerData.isMeditating() ? "§a修炼中" : "§c空闲"));
        lore.add("");
        lore.add("§e点击查看详情");
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建打坐按钮
     */
    private ItemStack createMeditateButtonItem(PlayerData playerData) {
        Material material = playerData.isMeditating() ? Material.REDSTONE_TORCH : Material.TORCH;
        String displayName = playerData.isMeditating() ? "§c停止打坐" : "§a开始打坐";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7" + (playerData.isMeditating() ? "停止当前的修炼" : "开始修炼获得修为"));
        lore.add("");
        if (playerData.isMeditating()) {
            lore.add("§e点击停止修炼");
        } else {
            lore.add("§e点击开始修炼");
        }
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建状态详情按钮
     */
    private ItemStack createStatsButtonItem() {
        Material material = Material.WRITABLE_BOOK;
        String displayName = "§b详细状态";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7查看详细的修仙信息");
        lore.add("§7包括境界、修为、属性等");
        lore.add("");
        lore.add("§e点击查看详情");
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建排行榜按钮
     */
    private ItemStack createRankingsButtonItem() {
        Material material = Material.GOLDEN_HELMET;
        String displayName = "§6修仙排行";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7查看服务器修仙排行榜");
        lore.add("§7看看谁是最强修仙者");
        lore.add("");
        lore.add("§e点击查看排行");
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建功法按钮
     */
    private ItemStack createKungFuButtonItem() {
        Material material = Material.ENCHANTED_BOOK;
        String displayName = "§d功法系统";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7学习和使用各种功法");
        lore.add("§7提升战斗力和生存能力");
        lore.add("");
        lore.add("§e点击进入功法系统");
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建境界信息按钮
     */
    private ItemStack createRealmInfoButtonItem() {
        Material material = Material.NETHER_STAR;
        String displayName = "§e境界介绍";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7了解各个境界的特点");
        lore.add("§7和修炼要求");
        lore.add("");
        lore.add("§e点击查看境界体系");
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建关闭按钮
     */
    private ItemStack createCloseButtonItem() {
        Material material = Material.BARRIER;
        String displayName = "§c关闭界面";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7关闭当前界面");
        lore.add("");
        lore.add("§e点击关闭");
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建返回按钮
     */
    private ItemStack createBackButtonItem(String targetGui) {
        Material material = Material.ARROW;
        String displayName = "§7返回上级";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7返回到上一级界面");
        lore.add("");
        lore.add("§e点击返回");
        
        ItemStack item = createItem(material, displayName, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "gui_target"),
                org.bukkit.persistence.PersistentDataType.STRING,
                targetGui
            );
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * 创建增强版功法物品
     */
    private ItemStack createEnhancedKungFuItem(EnhancedKungFu kungFu, EnhancedPlayerData playerData) {
        Material material = getKungFuMaterial(kungFu.getType());
        String displayName = "§" + getKungFuColor(kungFu.getType()) + kungFu.getName();
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7类型: §f" + kungFu.getTypeDisplay());
        lore.add("§7等级: §f" + kungFu.getLevelDisplay());
        lore.add("§7系统: §f" + kungFu.getSystemTypeDisplay());
        lore.add("§7境界要求: §f" + kungFu.getRealmRequirement());
        lore.add("§7修为要求: §f" + kungFu.getExpRequirement());
        if (kungFu.getLingliCost() > 0) {
            lore.add("§7灵力消耗: §f" + kungFu.getLingliCost());
        }
        if (kungFu.getNeiliCost() > 0) {
            lore.add("§7内力消耗: §f" + kungFu.getNeiliCost());
        }
        lore.add("");
        lore.add("§7" + kungFu.getDescription());
        lore.add("");
        
        // 检查是否满足学习条件
        if (kungFu.canLearn(playerData)) {
            lore.add("§a✓ 满足学习条件");
            lore.add("§e点击学习此功法");
        } else {
            lore.add("§c✗ 不满足学习条件");
            lore.add("§7需要更高的境界或修为");
        }
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建境界信息物品
     */
    private ItemStack createRealmInfoItem(String realmKey, String realmName, Material material) {
        String displayName = "§e" + realmName + "境界";
        
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add("§7境界ID: §f" + realmKey);
        lore.add("§7特点: §f待补充");
        lore.add("§7修炼要求: §f待补充");
        lore.add("");
        lore.add("§e点击查看详细信息");
        
        return createItem(material, displayName, lore);
    }
    
    /**
     * 创建基础物品
     */
    private ItemStack createItem(Material material, String displayName, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 根据功法类型获取对应材质
     */
    private Material getKungFuMaterial(String type) {
        switch (type.toLowerCase()) {
            case "attack": return Material.DIAMOND_SWORD;
            case "defense": return Material.SHIELD;
            case "support": return Material.FEATHER;
            case "healing": return Material.GLISTERING_MELON_SLICE;
            default: return Material.BOOK;
        }
    }
    
    /**
     * 根据功法类型获取颜色代码
     */
    private char getKungFuColor(String type) {
        switch (type.toLowerCase()) {
            case "attack": return 'c';  // 红色
            case "defense": return 'b'; // 蓝色
            case "support": return 'a'; // 绿色
            case "healing": return 'd'; // 紫色
            default: return 'f';        // 白色
        }
    }
    
    /**
     * 设置GUI打开状态
     */
    private void setOpen(Player player, String guiType) {
        openGuis.put(player.getUniqueId(), guiType);
    }
    
    /**
     * 检查GUI是否已打开
     */
    public boolean isOpen(Player player) {
        return openGuis.containsKey(player.getUniqueId());
    }
    
    /**
     * 关闭GUI
     */
    public void closeGui(Player player) {
        openGuis.remove(player.getUniqueId());
        player.closeInventory();
    }
    
    /**
     * 获取当前打开的GUI类型
     */
    public String getCurrentGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }
    
    /**
     * 处理GUI点击事件
     */
    public void handleClick(Player player, int slot, String guiType) {
        switch (guiType) {
            case "main":
                handleMainGuiClick(player, slot);
                break;
            case "kungfu":
                handleKungFuGuiClick(player, slot);
                break;
            case "realm_info":
                handleRealmInfoGuiClick(player, slot);
                break;
        }
    }
    
    /**
     * 处理主界面点击
     */
    private void handleMainGuiClick(Player player, int slot) {
        switch (slot) {
            case 10: // 打坐按钮
                PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
                playerData.toggleMeditation(player);
                player.sendMessage(playerData.isMeditating() ? 
                    "§a开始打坐修炼！" : "§c停止打坐修炼。");
                closeGui(player);
                break;
            case 11: // 详细状态
                player.performCommand("xiuzhen stats");
                closeGui(player);
                break;
            case 12: // 排行榜
                player.performCommand("xiuzhen top");
                closeGui(player);
                break;
            case 14: // 功法系统
                openKungFuGui(player);
                break;
            case 15: // 境界信息
                openRealmInfoGui(player);
                break;
            case 16: // 关闭界面
                closeGui(player);
                break;
        }
    }
    
    /**
     * 处理功法界面点击
     */
    private void handleKungFuGuiClick(Player player, int slot) {
        if (slot == 49) { // 返回按钮
            openMainGui(player);
            return;
        }
        
        // 处理功法点击（需要根据实际功法数据实现）
        // 这里暂时只处理返回按钮
    }
    
    /**
     * 处理境界信息界面点击
     */
    private void handleRealmInfoGuiClick(Player player, int slot) {
        if (slot == 22) { // 返回按钮
            openMainGui(player);
        }
    }
}
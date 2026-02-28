package com.adlamb.simplexiuzhen.ui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.lang.LanguageManager;

/**
 * 箱子UI管理器
 * 提供图形化的修仙信息展示界面
 */
public class ChestUIManager implements Listener {
    private final SimpleXiuzhen plugin;
    private final LanguageManager languageManager;
    
    // UI相关常量
    private static final int INVENTORY_SIZE = 27; // 3行箱子
    private static final String INVENTORY_TITLE = ChatColor.GOLD + "修仙信息面板";
    private static final String UI_IDENTIFIER = "simplexiuzhen_ui"; // UI标识符
    
    // 物品槽位定义
    private static final int REALM_SLOT = 10;      // 境界显示槽位
    private static final int EXP_SLOT = 11;        // 修为显示槽位
    private static final int STATUS_SLOT = 12;     // 状态显示槽位
    private static final int ATTRIBUTES_SLOT = 14; // 属性加成槽位
    private static final int ACTIONS_SLOT = 16;    // 操作按钮槽位
    private static final int DECORATION_START = 0; // 装饰物品起始位置

    public ChestUIManager(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 打开玩家修仙信息面板
     */
    public void openPlayerInfoPanel(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE);
        
        // 添加UI标识符到第一个装饰物品的lore中
        ItemStack identifierItem = createItem(Material.PAPER, "", null);
        ItemMeta meta = identifierItem.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add(UI_IDENTIFIER);
            meta.setLore(lore);
            identifierItem.setItemMeta(meta);
        }
        inventory.setItem(DECORATION_START, identifierItem);
        
        // 设置装饰边框
        setupDecoration(inventory);
        
        // 设置主要信息物品
        setupMainInfoItems(inventory, player, playerData);
        
        // 设置操作按钮
        setupActionButtons(inventory, player);
        
        // 打开界面
        player.openInventory(inventory);
    }

    /**
     * 设置装饰边框
     */
    private void setupDecoration(Inventory inventory) {
        ItemStack decorationItem = createItem(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        
        // 填充边框位置
        int[] decorationSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        for (int slot : decorationSlots) {
            inventory.setItem(slot, decorationItem);
        }
    }

    /**
     * 设置主要信息物品
     */
    private void setupMainInfoItems(Inventory inventory, Player player, PlayerData playerData) {
        // 境界信息
        ItemStack realmItem = createRealmItem(playerData);
        inventory.setItem(REALM_SLOT, realmItem);
        
        // 修为信息
        ItemStack expItem = createExpItem(playerData);
        inventory.setItem(EXP_SLOT, expItem);
        
        // 状态信息
        ItemStack statusItem = createStatusItem(playerData);
        inventory.setItem(STATUS_SLOT, statusItem);
        
        // 属性加成
        ItemStack attributesItem = createAttributesItem(playerData);
        inventory.setItem(ATTRIBUTES_SLOT, attributesItem);
    }

    /**
     * 设置操作按钮
     */
    private void setupActionButtons(Inventory inventory, Player player) {
        // 开始/停止打坐按钮
        ItemStack meditateButton = createMeditateButton(player);
        inventory.setItem(ACTIONS_SLOT, meditateButton);
    }

    /**
     * 创建境界显示物品
     */
    private ItemStack createRealmItem(PlayerData playerData) {
        String realmKey = playerData.getCurrentRealmKey();
        int subLevelIndex = playerData.getCurrentSubLevelIndex();
        
        // 从配置获取显示名称
        String realmDisplayName = plugin.getConfigManager().getRealmsConfig()
            .getString("realms." + realmKey + ".display_name", "未知境界");
        
        String subLevelName = getSubLevelName(realmKey, subLevelIndex);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "当前境界等级");
        lore.add("");
        lore.add(ChatColor.YELLOW + "境界: " + ChatColor.WHITE + realmDisplayName);
        lore.add(ChatColor.YELLOW + "段位: " + ChatColor.WHITE + subLevelName);
        
        return createItem(Material.NETHER_STAR, ChatColor.GOLD + "境界信息", lore);
    }

    /**
     * 创建修为显示物品
     */
    private ItemStack createExpItem(PlayerData playerData) {
        double currentExp = playerData.getCurrentExp();
        int expRequirement = getNextLevelExpRequirement(playerData);
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "修为进度");
        lore.add("");
        
        if (expRequirement == -1) {
            lore.add(ChatColor.GREEN + "修为: " + ChatColor.WHITE + String.format("%.2f", currentExp));
            lore.add(ChatColor.GREEN + "状态: " + ChatColor.WHITE + "已满级");
        } else {
            lore.add(ChatColor.GREEN + "修为: " + ChatColor.WHITE + String.format("%.2f", currentExp) + "/" + expRequirement);
            if (expRequirement > 0) {
                double progress = (currentExp / expRequirement) * 100;
                lore.add(ChatColor.YELLOW + "进度: " + ChatColor.WHITE + String.format("%.1f", progress) + "%");
            }
        }
        
        return createItem(Material.EXPERIENCE_BOTTLE, ChatColor.AQUA + "修为信息", lore);
    }

    /**
     * 创建状态显示物品
     */
    private ItemStack createStatusItem(PlayerData playerData) {
        boolean isMeditating = playerData.isMeditating();
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "当前状态");
        lore.add("");
        
        if (isMeditating) {
            lore.add(ChatColor.GREEN + "状态: " + ChatColor.WHITE + "正在打坐");
            lore.add(ChatColor.YELLOW + "提示: " + ChatColor.WHITE + "保持静止以获得修为");
        } else {
            lore.add(ChatColor.RED + "状态: " + ChatColor.WHITE + "未打坐");
            lore.add(ChatColor.YELLOW + "提示: " + ChatColor.WHITE + "使用 /xiuzhen meditate 开始修炼");
        }
        
        Material material = isMeditating ? Material.ENCHANTED_GOLDEN_APPLE : Material.APPLE;
        String displayName = isMeditating ? ChatColor.GREEN + "修炼中" : ChatColor.RED + "休息中";
        
        return createItem(material, displayName, lore);
    }

    /**
     * 创建属性加成物品
     */
    private ItemStack createAttributesItem(PlayerData playerData) {
        String realmKey = playerData.getCurrentRealmKey();
        int subLevelIndex = playerData.getCurrentSubLevelIndex();
        
        // 计算属性加成（示例数据）
        double healthBonus = subLevelIndex * 2.0;
        int manaBonus = subLevelIndex * 5;
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "境界属性加成");
        lore.add("");
        
        if (healthBonus > 0) {
            lore.add(ChatColor.LIGHT_PURPLE + "+" + healthBonus + " " + ChatColor.WHITE + "最大生命值");
        }
        if (manaBonus > 0) {
            lore.add(ChatColor.LIGHT_PURPLE + "+" + manaBonus + " " + ChatColor.WHITE + "最大灵力");
        }
        
        if (healthBonus <= 0 && manaBonus <= 0) {
            lore.add(ChatColor.GRAY + "当前境界无属性加成");
        }
        
        return createItem(Material.DIAMOND_CHESTPLATE, ChatColor.LIGHT_PURPLE + "属性加成", lore);
    }

    /**
     * 创建打坐操作按钮
     */
    private ItemStack createMeditateButton(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        boolean isMeditating = playerData.isMeditating();
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "点击进行操作");
        lore.add("");
        
        if (isMeditating) {
            lore.add(ChatColor.RED + "▶ 点击停止打坐");
            lore.add(ChatColor.YELLOW + "当前正在获得修为加成");
        } else {
            lore.add(ChatColor.GREEN + "▶ 点击开始打坐");
            lore.add(ChatColor.YELLOW + "开始修炼获得修为");
        }
        
        Material material = isMeditating ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;
        String displayName = isMeditating ? ChatColor.RED + "停止打坐" : ChatColor.GREEN + "开始打坐";
        
        return createItem(material, displayName, lore);
    }

    /**
     * 创建物品的通用方法
     */
    private ItemStack createItem(Material material, String displayName, List<String> lore) {
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
     * 获取段位名称
     */
    private String getSubLevelName(String realmKey, int subLevelIndex) {
        try {
            List<?> subLevels = plugin.getConfigManager().getRealmsConfig()
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

    /**
     * 获取下一等级修为要求
     */
    private int getNextLevelExpRequirement(PlayerData playerData) {
        String realmKey = playerData.getCurrentRealmKey();
        int subLevelIndex = playerData.getCurrentSubLevelIndex();
        
        try {
            List<?> subLevels = plugin.getConfigManager().getRealmsConfig()
                .getMapList("realms." + realmKey + ".sub_levels");
            
            int nextSubLevelIndex = subLevelIndex + 1;
            if (nextSubLevelIndex < subLevels.size()) {
                Object subLevelObj = subLevels.get(nextSubLevelIndex);
                if (subLevelObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> subLevel = (java.util.Map<String, Object>) subLevelObj;
                    Object expObj = subLevel.get("required_exp");
                    if (expObj instanceof Number) {
                        return ((Number) expObj).intValue();
                    }
                }
            } else {
                return -1; // 已满级
            }
        } catch (Exception e) {
            plugin.getLogger().warning("获取修为要求失败: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 检查是否是我们插件的UI
     */
    private boolean isOurUI(Inventory inventory) {
        ItemStack identifierItem = inventory.getItem(DECORATION_START);
        if (identifierItem != null && identifierItem.hasItemMeta()) {
            ItemMeta meta = identifierItem.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = meta.getLore();
                return lore != null && lore.contains(UI_IDENTIFIER);
            }
        }
        return false;
    }

    /**
     * 处理箱子点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        // 检查是否是我们的UI
        if (!isOurUI(inventory)) {
            return;
        }
        
        event.setCancelled(true); // 取消物品移动
        
        int slot = event.getRawSlot();
        
        // 处理操作按钮点击
        if (slot == ACTIONS_SLOT) {
            handleActionButtonClick(player);
        }
        // 其他槽位可以添加更多交互功能
    }

    /**
     * 处理操作按钮点击
     */
    private void handleActionButtonClick(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        boolean isMeditating = playerData.isMeditating();
        
        // 切换打坐状态
        playerData.toggleMeditation(player);
        
        if (playerData.isMeditating()) {
            player.sendMessage(languageManager.getPlayerMessage("meditate_start"));
            player.sendMessage(languageManager.getPlayerMessage("meditate_start_tip"));
        } else {
            player.sendMessage(languageManager.getPlayerMessage("meditate_stop"));
        }
        
        // 关闭并重新打开UI以更新显示
        player.closeInventory();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            openPlayerInfoPanel(player);
        }, 1L);
    }

    /**
     * 处理箱子关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // 可以在这里添加关闭时的处理逻辑
        // 比如保存数据或清理临时状态
    }
}
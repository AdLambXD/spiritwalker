package com.adlamb.simplexiuzhen.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * GUI事件监听器
 * 处理GUI界面的点击和关闭事件
 */
public class GuiListener implements Listener {
    private final SimpleXiuzhen plugin;
    private final GuiManager guiManager;
    
    public GuiListener(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        
        // 只处理玩家自己的背包点击
        if (inventory == null || !inventory.equals(player.getOpenInventory().getTopInventory())) {
            return;
        }
        
        // 检查是否是我们创建的GUI
        String guiType = guiManager.getCurrentGui(player);
        if (guiType == null) {
            return;
        }
        
        // 取消默认的物品移动行为
        event.setCancelled(true);
        
        int slot = event.getRawSlot();
        
        // 处理GUI点击
        guiManager.handleClick(player, slot, guiType);
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // 当GUI关闭时，清除打开状态记录
        if (guiManager.isOpen(player)) {
            guiManager.closeGui(player);
        }
    }
}
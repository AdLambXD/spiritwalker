package com.adlamb.simplexiuzhen.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 玩家退出监听器
 * 确保玩家下线时保存数据
 */
public class PlayerQuitListener implements Listener {
    private final SimpleXiuzhen plugin;

    public PlayerQuitListener(SimpleXiuzhen plugin) {
        this.plugin = plugin;
    }

    /**
     * 监听玩家退出事件，保存玩家数据
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // 保存玩家数据（包含修仙和武者双系统）
        if (plugin.getPlayerDataMap().containsKey(playerId)) {
            plugin.getPlayerData(playerId).saveData();
            plugin.removePlayerData(playerId);
        }
        
        plugin.getLogger().fine("玩家 " + event.getPlayer().getName() + " 数据已保存");
    }
}

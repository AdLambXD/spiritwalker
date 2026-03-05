package com.adlamb.simplexiuzhen.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 骑乘冥想监听器
 * 监听玩家骑乘无AI生物来触发打坐状态
 * 替代原有的复杂座位检测系统
 */
public class RideMeditationListener implements Listener {
    private final SimpleXiuzhen plugin;
    private final Set<UUID> ridingPlayers;
    private final Set<UUID> meditationCooldown; // 防止频繁切换的冷却
    
    // 支持冥想的实体类型（主要使用盔甲架）
    private static final EntityType[] MEDITATION_ENTITIES = {
        EntityType.ARMOR_STAND
    };

    public RideMeditationListener(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.ridingPlayers = new HashSet<>();
        this.meditationCooldown = new HashSet<>();
        
        // 启动骑乘状态检测任务
        startRideDetectionTask();
    }

    /**
     * 启动骑乘检测定时任务
     * 定期检查玩家是否骑乘着无AI生物
     */
    private void startRideDetectionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    PlayerData playerData = plugin.getPlayerData(playerId);
                    
                    // 检测玩家是否骑乘着无AI生物
                    if (isPlayerRidingMeditationEntity(player) && !ridingPlayers.contains(playerId)) {
                        handlePlayerStartRiding(player, playerData);
                    } else if (!isPlayerRidingMeditationEntity(player) && ridingPlayers.contains(playerId)) {
                        handlePlayerStopRiding(player, playerData);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 10L); // 每0.5秒检测一次
    }

    /**
     * 判断玩家是否骑乘着盔甲架
     * 盔甲架天生就是无AI实体，非常适合用于冥想
     */
    private boolean isPlayerRidingMeditationEntity(Player player) {
        if (!player.isInsideVehicle()) {
            return false;
        }
        
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return false;
        }
        
        // 检查是否是盔甲架
        return vehicle.getType() == EntityType.ARMOR_STAND;
    }



    /**
     * 处理玩家开始骑乘事件
     */
    private void handlePlayerStartRiding(Player player, PlayerData playerData) {
        UUID playerId = player.getUniqueId();
        
        // 检查冷却时间
        if (meditationCooldown.contains(playerId)) {
            return;
        }
        
        ridingPlayers.add(playerId);
        
        // 如果玩家不在打坐状态，则自动开始打坐
        if (!playerData.isMeditating()) {
            playerData.setMeditating(true);
            playerData.updateLastLocation(player.getLocation());
            
            player.sendMessage(Component.text("你骑乘着冥想盔甲架，自动进入打坐状态！", NamedTextColor.GREEN));
            player.sendMessage(Component.text("保持静止以获得修为加成...", NamedTextColor.YELLOW));
            
            // 播放打坐音效
            player.playSound(player.getLocation(), "entity.experience_orb.pickup", 0.5f, 1.5f);
            
            // 可以在这里添加粒子效果或其他视觉反馈
        }
    }

    /**
     * 处理玩家停止骑乘事件
     */
    private void handlePlayerStopRiding(Player player, PlayerData playerData) {
        UUID playerId = player.getUniqueId();
        
        ridingPlayers.remove(playerId);
        
        // 如果玩家在打坐状态，则停止打坐
        if (playerData.isMeditating()) {
            playerData.setMeditating(false);
            player.sendMessage(Component.text("你离开了冥想盔甲架，打坐状态结束。", NamedTextColor.RED));
            
            // 添加冷却时间防止频繁切换
            meditationCooldown.add(playerId);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                meditationCooldown.remove(playerId);
            }, 40L); // 2秒冷却
        }
    }

    /**
     * 监听玩家进入载具事件
     */
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntered();
        Entity vehicle = event.getVehicle();
        
        // 检查载具是否是我们的目标实体
        for (EntityType entityType : MEDITATION_ENTITIES) {
            if (vehicle.getType() == entityType) {
                PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
                handlePlayerStartRiding(player, playerData);
                break;
            }
        }
    }

    /**
     * 监听玩家离开载具事件
     */
    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getExited();
        UUID playerId = player.getUniqueId();
        
        // 检查玩家是否之前在骑乘状态
        if (ridingPlayers.contains(playerId)) {
            PlayerData playerData = plugin.getPlayerData(playerId);
            handlePlayerStopRiding(player, playerData);
        }
    }

    /**
     * 监听潜行切换事件（备用检测方式）
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        PlayerData playerData = plugin.getPlayerData(playerId);
        
        if (event.isSneaking()) {
            // 开始潜行时检查是否骑乘着合适实体
            if (!ridingPlayers.contains(playerId) && isPlayerRidingMeditationEntity(player)) {
                handlePlayerStartRiding(player, playerData);
            }
        } else {
            // 停止潜行时检查是否仍在骑乘
            if (ridingPlayers.contains(playerId) && !isPlayerRidingMeditationEntity(player)) {
                handlePlayerStopRiding(player, playerData);
            }
        }
    }

    /**
     * 检查玩家是否正在骑乘冥想实体
     */
    public boolean isPlayerRiding(UUID playerId) {
        return ridingPlayers.contains(playerId);
    }

    /**
     * 强制让玩家开始骑乘冥想（供其他插件调用）
     */
    public void forceStartMeditationRide(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        handlePlayerStartRiding(player, playerData);
    }

    /**
     * 强制让玩家停止骑乘冥想（供其他插件调用）
     */
    public void forceStopMeditationRide(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        handlePlayerStopRiding(player, playerData);
    }
    
    /**
     * 获取支持冥想的所有实体类型
     */
    public static EntityType[] getMeditationEntities() {
        return MEDITATION_ENTITIES.clone();
    }
}
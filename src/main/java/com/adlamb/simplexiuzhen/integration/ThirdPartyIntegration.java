package com.adlamb.simplexiuzhen.integration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 第三方插件集成器
 * 支持与 CMI、EssentialsX 等插件的集成
 */
public class ThirdPartyIntegration {
    private final SimpleXiuzhen plugin;
    private final Set<String> compatiblePlugins = new HashSet<>();
    
    // 可坐下的方块类型
    private static final Set<Material> SITTABLE_BLOCKS = new HashSet<>(Arrays.asList(
        Material.OAK_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS,
        Material.JUNGLE_STAIRS, Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS,
        Material.CRIMSON_STAIRS, Material.WARPED_STAIRS, Material.STONE_BRICK_STAIRS,
        Material.COBBLESTONE_STAIRS, Material.BRICK_STAIRS, Material.NETHER_BRICK_STAIRS,
        Material.SANDSTONE_STAIRS, Material.RED_SANDSTONE_STAIRS, Material.QUARTZ_STAIRS,
        Material.PURPUR_STAIRS, Material.PRISMARINE_STAIRS, Material.PRISMARINE_BRICK_STAIRS,
        Material.DARK_PRISMARINE_STAIRS, Material.POLISHED_GRANITE_STAIRS,
        Material.SMOOTH_RED_SANDSTONE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS,
        Material.POLISHED_DIORITE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS,
        Material.END_STONE_BRICK_STAIRS, Material.STONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS,
        Material.SMOOTH_QUARTZ_STAIRS, Material.GRANITE_STAIRS, Material.ANDESITE_STAIRS,
        Material.RED_NETHER_BRICK_STAIRS, Material.POLISHED_ANDESITE_STAIRS,
        Material.DIORITE_STAIRS, Material.BAMBOO_STAIRS, Material.CHERRY_STAIRS,
        Material.MANGROVE_STAIRS, Material.WHITE_CARPET, Material.ORANGE_CARPET,
        Material.MAGENTA_CARPET, Material.LIGHT_BLUE_CARPET, Material.YELLOW_CARPET,
        Material.LIME_CARPET, Material.PINK_CARPET, Material.GRAY_CARPET,
        Material.LIGHT_GRAY_CARPET, Material.CYAN_CARPET, Material.PURPLE_CARPET,
        Material.BLUE_CARPET, Material.BROWN_CARPET, Material.GREEN_CARPET,
        Material.RED_CARPET, Material.BLACK_CARPET, Material.LILY_PAD, Material.SNOW
    ));

    public ThirdPartyIntegration(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        detectCompatiblePlugins();
    }

    /**
     * 检测兼容的插件
     */
    private void detectCompatiblePlugins() {
        String[] pluginNames = {"CMI", "Essentials", "Sit"};
        
        for (String pluginName : pluginNames) {
            Plugin detectedPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (detectedPlugin != null && detectedPlugin.isEnabled()) {
                compatiblePlugins.add(pluginName);
                plugin.getLogger().info("检测到 " + pluginName + " 插件，已启用兼容模式");
            }
        }
        
        if (compatiblePlugins.isEmpty()) {
            plugin.getLogger().info("未检测到兼容的第三方插件");
        }
    }

    /**
     * 处理玩家坐下事件
     */
    public void handlePlayerSit(Player player) {
        if (compatiblePlugins.isEmpty()) return;
        
        player.sendMessage(ChatColor.GREEN + "[修仙系统] 检测到你坐下，自动开始打坐！");
        // 实际的打坐逻辑在 SeatListener 中处理
    }

    /**
     * 处理玩家起立事件
     */
    public void handlePlayerStand(Player player) {
        if (compatiblePlugins.isEmpty()) return;
        
        player.sendMessage(ChatColor.RED + "[修仙系统] 你站起来了，打坐结束。");
        // 实际的停止打坐逻辑在 SeatListener 中处理
    }

    /**
     * 检查玩家是否通过第三方插件坐下
     */
    public boolean isPlayerSeatedViaThirdParty(Player player) {
        if (compatiblePlugins.isEmpty()) return false;
        
        Block block = player.getLocation().getBlock();
        Material material = block.getType();
        
        // 检查是否在可坐下的方块上
        if (SITTABLE_BLOCKS.contains(material)) {
            return true;
        }
        
        // 特殊检查楼梯方向
        if (material.data == Stairs.class) {
            return true;
        }
        
        return false;
    }

    /**
     * 执行兼容的坐下命令
     */
    public boolean executeSitCommand(Player player) {
        if (compatiblePlugins.isEmpty()) return false;
        
        // 优先级：CMI > Essentials > 通用
        if (compatiblePlugins.contains("CMI")) {
            return executeCommand("cmi sit " + player.getName());
        } else if (compatiblePlugins.contains("Essentials")) {
            return executeCommand("e sit " + player.getName());
        } else {
            return executeCommand("sit " + player.getName());
        }
    }

    /**
     * 执行兼容的起立命令
     */
    public boolean executeStandCommand(Player player) {
        if (compatiblePlugins.isEmpty()) return false;
        
        // 优先级：CMI > Essentials > 通用
        if (compatiblePlugins.contains("CMI")) {
            return executeCommand("cmi stand " + player.getName());
        } else if (compatiblePlugins.contains("Essentials")) {
            return executeCommand("e stand " + player.getName());
        } else {
            return executeCommand("stand " + player.getName());
        }
    }

    /**
     * 执行命令的辅助方法
     */
    private boolean executeCommand(String command) {
        try {
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "执行命令失败: " + command, e);
            return false;
        }
    }

    /**
     * 获取可用的坐下命令列表（用于TAB补全）
     */
    public String[] getSitCommands() {
        return new String[]{"/sit", "/椅子"};
    }

    /**
     * 获取可用的起立命令列表（用于TAB补全）
     */
    public String[] getStandCommands() {
        return new String[]{"/stand", "/起身", "/起来"};
    }

    /**
     * 检查特定插件是否启用
     */
    public boolean isPluginEnabled(String pluginName) {
        return compatiblePlugins.contains(pluginName);
    }

    /**
     * 获取启用的插件列表
     */
    public String getEnabledPlugins() {
        return compatiblePlugins.isEmpty() ? "无" : String.join(", ", compatiblePlugins);
    }

    /**
     * 获取兼容插件数量
     */
    public int getCompatiblePluginCount() {
        return compatiblePlugins.size();
    }
}
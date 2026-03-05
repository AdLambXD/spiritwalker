package com.adlamb.simplexiuzhen.commands;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.adlamb.simplexiuzhen.EnhancedKungFu;
import com.adlamb.simplexiuzhen.EnhancedKungFuManager;
import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 功法命令处理器
 */
public class KungFuCommand implements CommandExecutor {
    private final SimpleXiuzhen plugin;
    private final EnhancedKungFuManager kungFuManager;
    
    public KungFuCommand(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.kungFuManager = plugin.getEnhancedKungFuManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("只有玩家可以使用此命令！", NamedTextColor.RED));
            return true;
        }
        
        Player player = (Player) sender;
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        
        if (args.length == 0) {
            showKungFuHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
                listKungFus(player);
                break;
            case "learn":
                learnKungFu(player, args);
                break;
            case "use":
                useKungFu(player, args);
                break;
            case "info":
                showKungFuInfo(player, args);
                break;
            default:
                showKungFuHelp(player);
                break;
        }
        
        return true;
    }
    
    /**
     * 显示功法帮助
     */
    private void showKungFuHelp(Player player) {
        player.sendMessage(Component.text("=== 功法系统 ===", NamedTextColor.GOLD));
        player.sendMessage(NamedTextColor.AQUA + "/kungfu list" + NamedTextColor.WHITE + " - 查看可用功法");
        player.sendMessage(NamedTextColor.AQUA + "/kungfu learn <功法ID>" + NamedTextColor.WHITE + " - 学习功法");
        player.sendMessage(NamedTextColor.AQUA + "/kungfu use <功法ID>" + NamedTextColor.WHITE + " - 使用功法");
        player.sendMessage(NamedTextColor.AQUA + "/kungfu info <功法ID>" + NamedTextColor.WHITE + " - 查看功法信息");
    }
    
    /**
     * 列出可用功法
     */
    private void listKungFus(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        List<String> availableKungFus = new ArrayList<>();
        
        for (EnhancedKungFu kungFu : kungFuManager.getAllKungFus()) {
            if (kungFuManager.hasKungFu(player.getUniqueId(), kungFu.getId())) {
                availableKungFus.add(NamedTextColor.GREEN + kungFu.getName() + " [" + kungFu.getLevel() + "] (已学习)");
            } else if (kungFuManager.canLearnKungFu(player.getUniqueId(), kungFu.getId())) {
                availableKungFus.add(NamedTextColor.YELLOW + kungFu.getName() + " [" + kungFu.getLevel() + "] (可学习)");
            } else {
                availableKungFus.add(NamedTextColor.GRAY + kungFu.getName() + " [" + kungFu.getLevel() + "] (未满足条件)");
            }
        }
        
        if (availableKungFus.isEmpty()) {
            player.sendMessage(Component.text("没有可用的功法", NamedTextColor.RED));
            return;
        }
        
        player.sendMessage(Component.text("=== 可用功法 ===", NamedTextColor.GOLD));
        for (String kungFu : availableKungFus) {
            player.sendMessage(kungFu);
        }
    }
    
    /**
     * 学习功法
     */
    private void learnKungFu(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("请指定功法ID", NamedTextColor.RED));
            return;
        }
        
        String kungFuId = args[1];
        if (kungFuManager.learnKungFu(player.getUniqueId(), kungFuId)) {
            player.sendMessage(NamedTextColor.GREEN + "成功学习功法: " + kungFuManager.getKungFu(kungFuId).getName());
        } else {
            player.sendMessage(Component.text("无法学习该功法，请检查境界和修为要求", NamedTextColor.RED));
        }
    }
    
    /**
     * 使用功法
     */
    private void useKungFu(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("请指定功法ID", NamedTextColor.RED));
            return;
        }
        
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        String kungFuId = args[1];
        EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
        
        if (kungFu == null) {
            player.sendMessage(Component.text("找不到该功法", NamedTextColor.RED));
            return;
        }
        
        if (!kungFuManager.hasKungFu(player.getUniqueId(), kungFuId)) {
            player.sendMessage(Component.text("您还没有学习该功法", NamedTextColor.RED));
            return;
        }
        
        if (kungFuManager.isKungFuOnCooldown(player.getUniqueId(), kungFuId)) {
            player.sendMessage(Component.text("该功法还在冷却中", NamedTextColor.RED));
            return;
        }
        
        // 检查资源消耗
        if (!kungFu.canUse(playerData)) {
            if ("xiuzhen".equals(kungFu.getSystemType())) {
                player.sendMessage(NamedTextColor.RED + "灵力不足！需要 " + kungFu.getLingliCost() + " 点灵力");
            } else {
                player.sendMessage(NamedTextColor.RED + "内力不足！需要 " + kungFu.getNeiliCost() + " 点内力");
            }
            return;
        }
        
        // 使用功法
        if (kungFuManager.useKungFu(player.getUniqueId(), kungFuId)) {
            player.sendMessage(NamedTextColor.GREEN + "成功使用功法: " + kungFu.getName());
            player.sendMessage(NamedTextColor.YELLOW + "效果: " + String.join(", ", kungFu.getEffects()));
        } else {
            player.sendMessage(Component.text("使用功法失败", NamedTextColor.RED));
        }
    }
    
    /**
     * 查看功法信息
     */
    private void showKungFuInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("请指定功法ID", NamedTextColor.RED));
            return;
        }
        
        String kungFuId = args[1];
        EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
        
        if (kungFu == null) {
            player.sendMessage(Component.text("找不到该功法", NamedTextColor.RED));
            return;
        }
        
        player.sendMessage(Component.text("=== 功法信息 ===", NamedTextColor.GOLD));
        player.sendMessage(NamedTextColor.AQUA + "名称: " + NamedTextColor.WHITE + kungFu.getName());
        player.sendMessage(NamedTextColor.AQUA + "描述: " + NamedTextColor.WHITE + kungFu.getDescription());
        player.sendMessage(NamedTextColor.AQUA + "类型: " + NamedTextColor.WHITE + kungFu.getTypeDisplay());
        player.sendMessage(NamedTextColor.AQUA + "等级: " + NamedTextColor.WHITE + kungFu.getLevelDisplay());
        player.sendMessage(NamedTextColor.AQUA + "所属系统: " + NamedTextColor.WHITE + kungFu.getSystemTypeDisplay());
        player.sendMessage(NamedTextColor.AQUA + "境界要求: " + NamedTextColor.WHITE + kungFu.getRealmRequirement());
        player.sendMessage(NamedTextColor.AQUA + "修为要求: " + NamedTextColor.WHITE + kungFu.getExpRequirement());
        if (kungFu.getLingliCost() > 0) {
            player.sendMessage(NamedTextColor.AQUA + "灵力消耗: " + NamedTextColor.WHITE + kungFu.getLingliCost());
        }
        if (kungFu.getNeiliCost() > 0) {
            player.sendMessage(NamedTextColor.AQUA + "内力消耗: " + NamedTextColor.WHITE + kungFu.getNeiliCost());
        }
        player.sendMessage(NamedTextColor.AQUA + "冷却时间: " + NamedTextColor.WHITE + kungFu.getCooldownTicks() / 20 + "秒");
        player.sendMessage(NamedTextColor.AQUA + "效果: " + NamedTextColor.WHITE + String.join(", ", kungFu.getEffects()));
    }
}
package com.adlamb.simplexiuzhen.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.adlamb.simplexiuzhen.BreakthroughSystem;
import com.adlamb.simplexiuzhen.EnhancedKungFu;
import com.adlamb.simplexiuzhen.EnhancedKungFuManager;
import com.adlamb.simplexiuzhen.EnhancedPlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 武者系统命令处理器
 * 处理 /wushu 命令的各种子命令
 * 实现 TabCompleter 接口提供TAB补全功能
 */
public class WushuCommand implements CommandExecutor, TabCompleter {
    private final SimpleXiuzhen plugin;
    private final EnhancedKungFuManager kungFuManager;
    private final BreakthroughSystem breakthroughSystem;
    
    // 武者主命令列表
    private static final List<String> MAIN_COMMANDS = Arrays.asList(
        "train", "stats", "breakthrough", "kungfu", "info"
    );
    
    // 武者功法子命令
    private static final List<String> KUNGFU_SUBCOMMANDS = Arrays.asList(
        "list", "learn", "use", "info"
    );

    public WushuCommand(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.kungFuManager = plugin.getEnhancedKungFuManager();
        this.breakthroughSystem = plugin.getBreakthroughSystem();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        EnhancedPlayerData playerData = plugin.getEnhancedPlayerData(player.getUniqueId());
        
        if (args.length == 0) {
            // 显示武者系统帮助信息
            showWushuHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "train":
                return handleTrain(player, playerData, args);
            case "stats":
                return handleStats(player, playerData);
            case "breakthrough":
                return handleBreakthrough(player, playerData);
            case "kungfu":
                return handleKungFu(player, args);
            case "info":
                return handleInfo(player);
            default:
                player.sendMessage(ChatColor.RED + "未知的武者命令！使用 /wushu 查看帮助");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 补全主命令
            return filterCompletions(MAIN_COMMANDS, args[0]);
        } else if (args.length == 2 && "kungfu".equalsIgnoreCase(args[0])) {
            // 补全功法子命令
            return filterCompletions(KUNGFU_SUBCOMMANDS, args[1]);
        } else if (args.length == 3 && "kungfu".equalsIgnoreCase(args[0])) {
            // 补全功法ID（简化处理）
            if ("learn".equalsIgnoreCase(args[1]) || "use".equalsIgnoreCase(args[1]) || "info".equalsIgnoreCase(args[1])) {
                return Arrays.asList("sword_art", "iron_body", "tiger_fist");
            }
        }
        return Collections.emptyList();
    }

    /**
     * 过滤补全选项
     */
    private List<String> filterCompletions(List<String> options, String input) {
        if (input == null || input.isEmpty()) {
            return options;
        }
        
        List<String> filtered = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        
        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerInput)) {
                filtered.add(option);
            }
        }
        
        return filtered;
    }

    /**
     * 处理战斗训练命令
     */
    private boolean handleTrain(Player player, EnhancedPlayerData playerData, String[] args) {
        playerData.toggleCombatTraining(player);
        
        if (playerData.isInCombatTraining()) {
            player.sendMessage(ChatColor.GREEN + "开始武者战斗训练！");
            player.sendMessage(ChatColor.YELLOW + "通过战斗来提升内力和武者修为...");
            
            // 显示当前状态
            player.sendMessage(ChatColor.AQUA + "当前内力: " + ChatColor.WHITE + 
                String.format("%.1f", playerData.getCurrentNeili()) + "/" + 
                String.format("%.1f", playerData.getMaxNeili()));
            player.sendMessage(ChatColor.AQUA + "武者境界: " + ChatColor.WHITE + 
                playerData.getCurrentWushuRealmKey() + " 第" + 
                (playerData.getCurrentWushuSubLevelIndex() + 1) + "层");
        } else {
            player.sendMessage(ChatColor.RED + "停止武者战斗训练。");
        }
        
        return true;
    }

    /**
     * 处理武者状态查看
     */
    private boolean handleStats(Player player, EnhancedPlayerData playerData) {
        player.sendMessage(ChatColor.GOLD + "=== 武者系统状态 ===");
        player.sendMessage(ChatColor.AQUA + "境界: " + ChatColor.WHITE + 
            playerData.getCurrentWushuRealmKey() + " 第" + 
            (playerData.getCurrentWushuSubLevelIndex() + 1) + "层");
        player.sendMessage(ChatColor.AQUA + "武者修为: " + ChatColor.WHITE + 
            String.format("%.2f", playerData.getCurrentWushuExp()));
        player.sendMessage(ChatColor.AQUA + "内力值: " + ChatColor.WHITE + 
            String.format("%.1f", playerData.getCurrentNeili()) + "/" + 
            String.format("%.1f", playerData.getMaxNeili()));
        player.sendMessage(ChatColor.AQUA + "训练状态: " + 
            (playerData.isInCombatTraining() ? ChatColor.GREEN + "训练中" : ChatColor.RED + "未训练"));
        
        // 显示属性加成
        displayWushuAttributes(player, playerData);
        return true;
    }

    /**
     * 处理境界突破
     */
    private boolean handleBreakthrough(Player player, EnhancedPlayerData playerData) {
        if (breakthroughSystem.attemptWushuBreakthrough(player, playerData)) {
            player.sendMessage(ChatColor.GOLD + "武道突破成功！");
            // 自动显示新状态
            handleStats(player, playerData);
        } else {
            player.sendMessage(ChatColor.RED + "突破条件不满足或突破失败！");
        }
        return true;
    }

    /**
     * 处理武者功法相关命令
     */
    private boolean handleKungFu(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "请指定功法操作: list, learn, use, info");
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "list":
                listWushuKungFus(player);
                break;
            case "learn":
                if (args.length >= 3) {
                    learnWushuKungFu(player, args[2]);
                } else {
                    player.sendMessage(ChatColor.RED + "请指定功法ID");
                }
                break;
            case "use":
                if (args.length >= 3) {
                    useWushuKungFu(player, args[2]);
                } else {
                    player.sendMessage(ChatColor.RED + "请指定功法ID");
                }
                break;
            case "info":
                if (args.length >= 3) {
                    showWushuKungFuInfo(player, args[2]);
                } else {
                    player.sendMessage(ChatColor.RED + "请指定功法ID");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "未知的功法操作！使用: list, learn, use, info");
        }
        return true;
    }

    /**
     * 处理武者境界信息查看
     */
    private boolean handleInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 武者境界体系 ===");
        player.sendMessage(ChatColor.AQUA + "修炼境界 (Lv.1): " + ChatColor.WHITE + "初学 → 入门 → 小成 → 熟练");
        player.sendMessage(ChatColor.AQUA + "通脉境界 (Lv.2): " + ChatColor.WHITE + "初期 → 中期 → 后期 → 圆满");
        player.sendMessage(ChatColor.AQUA + "筑经境界 (Lv.3): " + ChatColor.WHITE + "初期 → 中期 → 后期 → 圆满");
        player.sendMessage(ChatColor.AQUA + "化元境界 (Lv.4): " + ChatColor.WHITE + "初期 → 中期 → 后期 → 圆满");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "提示: 武者系统通过战斗训练提升，内力是施展武技的关键资源");
        return true;
    }

    /**
     * 显示武者功法列表
     */
    private void listWushuKungFus(Player player) {
        EnhancedPlayerData playerData = plugin.getEnhancedPlayerData(player.getUniqueId());
        List<EnhancedKungFu> wushuKungFus = kungFuManager.getKungFusBySystem("wushu");
        
        player.sendMessage(ChatColor.GOLD + "=== 武者功法列表 ===");
        for (EnhancedKungFu kungFu : wushuKungFus) {
            String status = kungFuManager.hasKungFu(player.getUniqueId(), kungFu.getId()) ? 
                ChatColor.GREEN + "[已学习]" : 
                (kungFuManager.canLearnKungFu(player.getUniqueId(), kungFu.getId()) ? 
                    ChatColor.YELLOW + "[可学习]" : 
                    ChatColor.GRAY + "[未解锁]");
            
            player.sendMessage(status + " " + ChatColor.WHITE + kungFu.getName() + 
                " (" + kungFu.getLevelDisplay() + ") - " + kungFu.getDescription());
        }
    }

    /**
     * 学习武者功法
     */
    private void learnWushuKungFu(Player player, String kungFuId) {
        if (kungFuManager.learnKungFu(player.getUniqueId(), kungFuId)) {
            EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
            player.sendMessage(ChatColor.GREEN + "成功学习武者功法: " + kungFu.getName());
        } else {
            player.sendMessage(ChatColor.RED + "无法学习该武者功法，请检查境界和修为要求");
        }
    }

    /**
     * 使用武者功法
     */
    private void useWushuKungFu(Player player, String kungFuId) {
        EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
        if (kungFu == null || !"wushu".equals(kungFu.getSystemType())) {
            player.sendMessage(ChatColor.RED + "这不是有效的武者功法！");
            return;
        }

        if (kungFuManager.useKungFu(player.getUniqueId(), kungFuId)) {
            player.sendMessage(ChatColor.GREEN + "施展武者功法: " + kungFu.getName());
            player.sendMessage(ChatColor.YELLOW + "效果: " + String.join(", ", kungFu.getEffects()));
        } else {
            player.sendMessage(ChatColor.RED + "武者功法使用失败！");
        }
    }

    /**
     * 显示武者功法信息
     */
    private void showWushuKungFuInfo(Player player, String kungFuId) {
        EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
        if (kungFu == null || !"wushu".equals(kungFu.getSystemType())) {
            player.sendMessage(ChatColor.RED + "找不到该武者功法！");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== 武者功法详情 ===");
        player.sendMessage(ChatColor.AQUA + "名称: " + ChatColor.WHITE + kungFu.getName());
        player.sendMessage(ChatColor.AQUA + "类型: " + ChatColor.WHITE + kungFu.getTypeDisplay());
        player.sendMessage(ChatColor.AQUA + "等级: " + ChatColor.WHITE + kungFu.getLevelDisplay());
        player.sendMessage(ChatColor.AQUA + "内力消耗: " + ChatColor.WHITE + kungFu.getNeiliCost());
        player.sendMessage(ChatColor.AQUA + "冷却时间: " + ChatColor.WHITE + (kungFu.getCooldownTicks() / 20) + "秒");
        player.sendMessage(ChatColor.AQUA + "描述: " + ChatColor.WHITE + kungFu.getDescription());
    }

    /**
     * 显示武者属性加成
     */
    private void displayWushuAttributes(Player player, EnhancedPlayerData playerData) {
        int strengthBonus = playerData.getCurrentWushuSubLevelIndex() * 2;
        int defenseBonus = playerData.getCurrentWushuSubLevelIndex() * 1;
        
        if (strengthBonus > 0 || defenseBonus > 0) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "武者属性加成:");
            if (strengthBonus > 0) {
                player.sendMessage("  " + ChatColor.WHITE + "+ " + strengthBonus + " 力量");
            }
            if (defenseBonus > 0) {
                player.sendMessage("  " + ChatColor.WHITE + "+ " + defenseBonus + " 防御");
            }
        }
    }

    /**
     * 显示武者系统帮助
     */
    private void showWushuHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== 武者系统命令 ===");
        player.sendMessage(ChatColor.AQUA + "/wushu" + ChatColor.WHITE + " - 查看武者系统帮助");
        player.sendMessage(ChatColor.AQUA + "/wushu train" + ChatColor.WHITE + " - 开始/停止战斗训练");
        player.sendMessage(ChatColor.AQUA + "/wushu stats" + ChatColor.WHITE + " - 查看武者详细状态");
        player.sendMessage(ChatColor.AQUA + "/wushu breakthrough" + ChatColor.WHITE + " - 尝试境界突破");
        player.sendMessage(ChatColor.AQUA + "/wushu kungfu list" + ChatColor.WHITE + " - 查看可学武者功法");
        player.sendMessage(ChatColor.AQUA + "/wushu kungfu learn <ID>" + ChatColor.WHITE + " - 学习武者功法");
        player.sendMessage(ChatColor.AQUA + "/wushu kungfu use <ID>" + ChatColor.WHITE + " - 使用武者功法");
        player.sendMessage(ChatColor.AQUA + "/wushu info" + ChatColor.WHITE + " - 查看武者境界体系");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "武者系统专注于战斗训练和内力修炼");
        player.sendMessage(ChatColor.GRAY + "通过战斗获得经验和内力，使用武者功法需要消耗内力");
    }
}
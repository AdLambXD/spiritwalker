package com.adlamb.simplexiuzhen.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.adlamb.simplexiuzhen.ConfigManager;
import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.ranking.RankingManager;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * 玩家命令处理器
 * 处理 /xiuzhen 命令的各种子命令
 * 实现 TabCompleter 接口提供TAB补全功能
 */
public class XiuzhenCommand implements CommandExecutor, TabCompleter {
    private final SimpleXiuzhen plugin;
    private final ConfigManager configManager;
    private final RankingManager rankingManager;
    
    // 主命令列表
    private static final List<String> MAIN_COMMANDS = Arrays.asList(
        "meditate", "stats", "top", "info", "ui"
    );
    
    // 数字参数（用于分页）
    private static final List<String> NUMBER_ARGS = Arrays.asList("1", "2", "3", "4", "5");

    public XiuzhenCommand(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.rankingManager = new RankingManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // 显示帮助信息
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "meditate":
                return handleMeditate(sender, args);
            case "stats":
                return handleStats(sender, args);
            case "top":
                return handleTop(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "ui":
                return handleUI(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "未知的子命令！使用 /xiuzhen 查看帮助");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 补全主命令
            return filterCompletions(MAIN_COMMANDS, args[0]);
        } else if (args.length == 2) {
            // 根据不同主命令补全参数
            switch (args[0].toLowerCase()) {
                case "top":
                    return filterCompletions(NUMBER_ARGS, args[1]);
                case "ui":
                    // ui命令不需要额外参数
                    return Collections.emptyList();
                default:
                    return Collections.emptyList();
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
     * 处理打坐命令
     */
    private boolean handleMeditate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        playerData.toggleMeditation(player);

        if (playerData.isMeditating()) {
            player.sendMessage(ChatColor.GREEN + "开始打坐修炼！");
            player.sendMessage(ChatColor.YELLOW + "保持静止以获得修为...");
            
            // 发送交互式提示
            TextComponent component = new TextComponent(ChatColor.GOLD + "[点击停止打坐]");
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xiuzhen meditate"));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder("点击立即停止打坐").create()));
            player.spigot().sendMessage(component);
        } else {
            player.sendMessage(ChatColor.RED + "停止打坐修炼。");
        }
        return true;
    }

    /**
     * 处理状态查看命令
     */
    private boolean handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        displayDetailedStats(player, playerData);
        return true;
    }

    /**
     * 处理排行榜命令
     */
    private boolean handleTop(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "页码必须是数字！");
                return true;
            }
        }

        rankingManager.displayRankings(sender, page);
        return true;
    }

    /**
     * 处理境界信息命令
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        FileConfiguration realmsConfig = configManager.getRealmsConfig();
        
        sender.sendMessage(ChatColor.GOLD + "=== 境界体系介绍 ===");
        
        // 获取所有境界
        Set<String> realms = realmsConfig.getConfigurationSection("realms").getKeys(false);
        for (String realmKey : realms) {
            String displayName = realmsConfig.getString("realms." + realmKey + ".display_name", realmKey);
            int level = realmsConfig.getInt("realms." + realmKey + ".level", 0);
            
            sender.sendMessage(ChatColor.AQUA + displayName + " (等级 " + level + ")");
            
            // 安全显示段位信息
            try {
                List<Map<?, ?>> subLevels = realmsConfig.getMapList("realms." + realmKey + ".sub_levels");
                for (int i = 0; i < subLevels.size(); i++) {
                    Map<?, ?> subLevel = subLevels.get(i);
                    String name = (String) subLevel.get("name");
                    Object expObj = subLevel.get("required_exp");
                    int requiredExp = 0;
                    if (expObj instanceof Number) {
                        requiredExp = ((Number) expObj).intValue();
                    }
                    sender.sendMessage("  " + ChatColor.WHITE + "- " + name + ": 需要修为 " + requiredExp);
                }
            } catch (Exception e) {
                sender.sendMessage("  " + ChatColor.RED + "段位信息读取失败");
                plugin.getLogger().warning("读取境界" + realmKey + "的段位信息失败: " + e.getMessage());
            }
            sender.sendMessage("");
        }
        
        sender.sendMessage(ChatColor.YELLOW + "使用 /xiuzhen stats 查看个人状态");
        return true;
    }
    
    /**
     * 处理UI界面命令
     */
    private boolean handleUI(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getChestUIManager().openPlayerInfoPanel(player);
        return true;
    }

    /**
     * 显示详细状态信息
     */
    private void displayDetailedStats(Player player, PlayerData playerData) {
        FileConfiguration realmsConfig = configManager.getRealmsConfig();
        
        // 获取境界信息
        String realmKey = playerData.getCurrentRealmKey();
        String realmDisplayName = realmsConfig.getString("realms." + realmKey + ".display_name", "未知境界");
        int subLevelIndex = playerData.getCurrentSubLevelIndex();
        
        // 安全获取段位名称
        String subLevelName = "未知段位";
        try {
            List<Map<?, ?>> subLevels = realmsConfig.getMapList("realms." + realmKey + ".sub_levels");
            if (subLevelIndex >= 0 && subLevelIndex < subLevels.size()) {
                Map<?, ?> subLevel = subLevels.get(subLevelIndex);
                subLevelName = (String) subLevel.get("name");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("获取段位信息失败: " + e.getMessage());
        }
        
        // 获取修为要求（显示下一段位的要求）
        int expRequirement = 0;
        try {
            List<Map<?, ?>> subLevels = realmsConfig.getMapList("realms." + realmKey + ".sub_levels");
            int nextSubLevelIndex = subLevelIndex + 1;
            if (nextSubLevelIndex < subLevels.size()) {
                // 显示下一段位的要求
                Map<?, ?> nextSubLevel = subLevels.get(nextSubLevelIndex);
                Object expObj = nextSubLevel.get("required_exp");
                if (expObj instanceof Number) {
                    expRequirement = ((Number) expObj).intValue();
                }
            } else {
                // 已经是最高段位，显示当前境界的最高要求或特殊标识
                expRequirement = -1; // 特殊值表示已满级
            }
        } catch (Exception e) {
            plugin.getLogger().warning("获取修为要求失败: " + e.getMessage());
        }
        
        player.sendMessage(ChatColor.GOLD + "=== 修仙详细状态 ===");
        player.sendMessage(ChatColor.AQUA + "境界: " + ChatColor.WHITE + realmDisplayName + subLevelName);
        
        // 根据是否满级显示不同的修为信息
        if (expRequirement == -1) {
            player.sendMessage(ChatColor.AQUA + "修为: " + ChatColor.WHITE + String.format("%.2f", playerData.getCurrentExp()) + " (已满级)");
        } else {
            player.sendMessage(ChatColor.AQUA + "修为: " + ChatColor.WHITE + String.format("%.2f", playerData.getCurrentExp()) + "/" + expRequirement);
        }
        player.sendMessage(ChatColor.AQUA + "状态: " + (playerData.isMeditating() ? 
            ChatColor.GREEN + "打坐中" : ChatColor.RED + "未打坐"));
        
        // 显示属性加成（如果有）
        displayAttributeBonuses(player, realmKey, subLevelIndex);
        
        // 发送交互式菜单
        sendInteractiveMenu(player);
    }

    /**
     * 显示属性加成
     */
    private void displayAttributeBonuses(Player player, String realmKey, int subLevelIndex) {
        // 这里可以根据境界和段位显示对应的属性加成
        // 示例数据，实际应该从配置文件读取
        double healthBonus = subLevelIndex * 2.0;
        int manaBonus = subLevelIndex * 5;
        
        if (healthBonus > 0 || manaBonus > 0) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "属性加成:");
            if (healthBonus > 0) {
                player.sendMessage("  " + ChatColor.WHITE + "+ " + healthBonus + " 最大生命值");
            }
            if (manaBonus > 0) {
                player.sendMessage("  " + ChatColor.WHITE + "+ " + manaBonus + " 最大灵力");
            }
        }
    }

    /**
     * 发送交互式菜单
     */
    private void sendInteractiveMenu(Player player) {
        player.sendMessage(ChatColor.GRAY + "--- 快捷操作 ---");
        
        TextComponent meditateBtn = new TextComponent(ChatColor.GREEN + "[开始打坐] ");
        meditateBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xiuzhen meditate"));
        meditateBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("开始修炼获得修为").create()));
            
        TextComponent rankingsBtn = new TextComponent(ChatColor.AQUA + "[查看排行]");
        rankingsBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xiuzhen top"));
        rankingsBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("查看服务器修仙排行榜").create()));
            
        player.spigot().sendMessage(meditateBtn, rankingsBtn);
    }

    /**
     * 显示帮助信息
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== 修仙系统命令帮助 ===");
        
        // 从配置文件读取帮助信息
        List<String> playerCommands = plugin.getConfig().getStringList("messages.help.player_commands");
        if (!playerCommands.isEmpty()) {
            for (String cmd : playerCommands) {
                sender.sendMessage(ChatColor.AQUA + cmd);
            }
        } else {
            // 默认帮助信息
            sender.sendMessage(ChatColor.AQUA + "/xiuzhen" + ChatColor.WHITE + " - 查看基本信息");
            sender.sendMessage(ChatColor.AQUA + "/xiuzhen meditate" + ChatColor.WHITE + " - 开始/停止打坐");
            sender.sendMessage(ChatColor.AQUA + "/xiuzhen stats" + ChatColor.WHITE + " - 查看详细状态");
            sender.sendMessage(ChatColor.AQUA + "/xiuzhen top [页码]" + ChatColor.WHITE + " - 查看排行榜");
            sender.sendMessage(ChatColor.AQUA + "/xiuzhen info" + ChatColor.WHITE + " - 查看境界介绍");
        }
        
        if (sender.hasPermission("simplexiuzhen.admin")) {
            List<String> adminCommands = plugin.getConfig().getStringList("messages.help.admin_commands");
            if (!adminCommands.isEmpty()) {
                sender.sendMessage("");
                sender.sendMessage(ChatColor.RED + "管理员命令:");
                for (String cmd : adminCommands) {
                    sender.sendMessage(ChatColor.RED + cmd);
                }
            }
        }
        
        // 显示提示信息
        List<String> tips = plugin.getConfig().getStringList("messages.help.tips");
        if (!tips.isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GRAY + "提示:");
            for (String tip : tips) {
                sender.sendMessage(ChatColor.GRAY + "- " + tip);
            }
        }
        
        sender.sendMessage(ChatColor.GRAY + "别名: /xz, /xiuxian");
    }

    /**
     * 安全获取配置中的整数值
     */
    private int getConfigInt(FileConfiguration config, String path, int defaultValue) {
        Object value = config.get(path);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else {
            return defaultValue;
        }
    }
}
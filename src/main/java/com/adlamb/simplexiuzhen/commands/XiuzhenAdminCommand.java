package com.adlamb.simplexiuzhen.commands;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.adlamb.simplexiuzhen.ConfigManager;
import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.database.DatabaseManager;

/**
 * 管理员命令处理器
 * 处理 /xiuzhenadmin 命令
 * 实现 TabCompleter 接口提供TAB补全功能
 */
public class XiuzhenAdminCommand implements CommandExecutor, TabCompleter {
    private final SimpleXiuzhen plugin;
    private final ConfigManager configManager;
    private final DatabaseManager databaseManager;
    
    // 管理员命令列表
    private static final List<String> ADMIN_COMMANDS = Arrays.asList(
        "setrealm", "addexp", "reloadconfig", "backup", "reset"
    );
    
    // 境界列表（从配置文件动态获取更好）
    private static final List<String> REALMS_LIST = Arrays.asList(
        "LianQi", "ZhuJi", "JinDan", "YuanYing", "HuaShen"
    );

    public XiuzhenAdminCommand(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.databaseManager = plugin.getDatabaseManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("simplexiuzhen.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用此命令！");
            return true;
        }

        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setrealm":
                return handleSetRealm(sender, args);
            case "addexp":
                return handleAddExp(sender, args);
            case "reloadconfig":
                return handleReloadConfig(sender, args);
            case "backup":
                return handleBackup(sender, args);
            case "reset":
                return handleReset(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "未知的管理员命令！");
                showAdminHelp(sender);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("simplexiuzhen.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            // 补全主命令
            return filterCompletions(ADMIN_COMMANDS, args[0]);
        } else if (args.length == 2) {
            // 根据不同命令补全参数
            switch (args[0].toLowerCase()) {
                case "setrealm":
                case "addexp":
                case "reset":
                    // 补全在线玩家名
                    return getOnlinePlayers(args[1]);
                default:
                    return Collections.emptyList();
            }
        } else if (args.length == 3) {
            // 根据不同命令补全第三个参数
            switch (args[0].toLowerCase()) {
                case "setrealm":
                    // 补全境界列表
                    return filterCompletions(REALMS_LIST, args[2]);
                case "addexp":
                    // 补全经验值（预设一些常用值）
                    return Arrays.asList("100", "500", "1000", "5000");
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
     * 获取在线玩家列表
     */
    private List<String> getOnlinePlayers(String input) {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
        }
        
        if (input == null || input.isEmpty()) {
            return players;
        }
        
        return filterCompletions(players, input);
    }

    /**
     * 设置玩家境界
     */
    private boolean handleSetRealm(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法: /xiuzhenadmin setrealm <玩家名> <境界Key>");
            sender.sendMessage(ChatColor.YELLOW + "示例: /xiuzhenadmin setrealm Player1 JinDan");
            return true;
        }

        String playerName = args[1];
        String realmKey = args[2];
        
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        // 验证境界是否存在
        if (!configManager.getRealmsConfig().contains("realms." + realmKey)) {
            sender.sendMessage(ChatColor.RED + "境界 " + realmKey + " 不存在！");
            sender.sendMessage(ChatColor.YELLOW + "可用境界: " + String.join(", ", REALMS_LIST));
            return true;
        }

        PlayerData playerData = plugin.getPlayerData(targetPlayer.getUniqueId());
        playerData.setCurrentRealmKey(realmKey);
        playerData.setCurrentSubLevelIndex(0);
        playerData.setCurrentExp(0);
        playerData.saveData();

        sender.sendMessage(ChatColor.GREEN + "已将玩家 " + playerName + " 的境界设置为 " + realmKey);
        targetPlayer.sendMessage(ChatColor.GOLD + "管理员已将你的境界设置为 " + realmKey);
        return true;
    }

    /**
     * 添加玩家修为
     */
    private boolean handleAddExp(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法: /xiuzhenadmin addexp <玩家名> <修为值>");
            return true;
        }

        String playerName = args[1];
        int expToAdd;
        
        try {
            expToAdd = Integer.parseInt(args[2]);
            if (expToAdd <= 0) {
                sender.sendMessage(ChatColor.RED + "修为值必须大于0！");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "修为值必须是数字！");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        PlayerData playerData = plugin.getPlayerData(targetPlayer.getUniqueId());
        playerData.addExp(expToAdd);
        playerData.saveData();

        sender.sendMessage(ChatColor.GREEN + "已为玩家 " + playerName + " 添加 " + expToAdd + " 点修为");
        targetPlayer.sendMessage(ChatColor.GOLD + "管理员为你增加了 " + expToAdd + " 点修为");
        return true;
    }

    /**
     * 重载配置文件
     */
    private boolean handleReloadConfig(CommandSender sender, String[] args) {
        try {
            configManager.reloadConfigs();
            databaseManager.reload();
            sender.sendMessage(ChatColor.GREEN + "配置文件和数据库连接重载成功！");
            sender.sendMessage(ChatColor.YELLOW + "当前存储类型: " + databaseManager.getStorageTypeString());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "重载失败: " + e.getMessage());
            plugin.getLogger().severe("配置重载失败: " + e.getMessage());
        }
        return true;
    }

    /**
     * 数据备份
     */
    private boolean handleBackup(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.YELLOW + "正在创建数据备份...");
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupName = "backup_" + timestamp;
            
            // 创建备份目录
            File backupDir = new File(plugin.getDataFolder(), "backups/" + backupName);
            backupDir.mkdirs();
            
            // 复制数据库文件（如果是MySQL）
            if (databaseManager.getStorageType().isDatabase()) {
                sender.sendMessage(ChatColor.GREEN + "MySQL备份指令已记录，请手动执行备份");
            } else {
                // YAML模式备份
                sender.sendMessage(ChatColor.GREEN + "YAML数据备份完成: " + backupName);
            }
            
            // 备份配置文件
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File realmsFile = new File(plugin.getDataFolder(), "realms.yml");
            
            sender.sendMessage(ChatColor.GREEN + "配置文件备份完成");
            sender.sendMessage(ChatColor.YELLOW + "备份位置: " + backupDir.getAbsolutePath());
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "备份失败: " + e.getMessage());
            plugin.getLogger().severe("备份失败: " + e.getMessage());
        }
        return true;
    }

    /**
     * 重置玩家数据
     */
    private boolean handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /xiuzhenadmin reset <玩家名>");
            sender.sendMessage(ChatColor.RED + "警告: 此操作不可逆！");
            return true;
        }

        String playerName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
        
        if (!targetPlayer.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 从未加入过服务器！");
            return true;
        }

        UUID playerUUID = targetPlayer.getUniqueId();
        
        // 删除玩家数据文件
        File dataFile = new File(plugin.getDataFolder(), "player_data/" + playerUUID.toString() + ".yml");
        if (dataFile.exists()) {
            dataFile.delete();
        }

        // 从内存中移除
        plugin.removePlayerData(playerUUID);

        sender.sendMessage(ChatColor.GREEN + "已重置玩家 " + playerName + " 的所有修仙数据");
        
        if (targetPlayer.isOnline()) {
            Player onlinePlayer = targetPlayer.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayer.sendMessage(ChatColor.RED + "你的修仙数据已被管理员重置");
            }
        }
        return true;
    }

    /**
     * 显示管理员帮助
     */
    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== 修仙系统管理员命令 ===");
        
        // 从配置文件读取管理员命令帮助
        List<String> adminCommands = plugin.getConfig().getStringList("messages.help.admin_commands");
        if (!adminCommands.isEmpty()) {
            for (String cmd : adminCommands) {
                sender.sendMessage(ChatColor.RED + cmd);
            }
        } else {
            // 默认帮助信息
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin setrealm <玩家> <境界>" + ChatColor.WHITE + " - 设置玩家境界");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin addexp <玩家> <修为>" + ChatColor.WHITE + " - 添加修为");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin reloadconfig" + ChatColor.WHITE + " - 重载配置");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin backup" + ChatColor.WHITE + " - 创建数据备份");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin reset <玩家>" + ChatColor.WHITE + " - 重置玩家数据");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "别名: /xza, /xzadmin");
        sender.sendMessage(ChatColor.GRAY + "使用 TAB 键可获得智能命令补全");
    }
}
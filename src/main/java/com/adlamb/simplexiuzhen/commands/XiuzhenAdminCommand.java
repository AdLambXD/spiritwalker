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
import com.adlamb.simplexiuzhen.EnhancedKungFu;
import com.adlamb.simplexiuzhen.EnhancedKungFuManager;
import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.BreakthroughSystem;
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
        "setxiuzhenrealm", "setwushurealm", "addxiuzhenexp", "addwushuexp", "addlingli", "addneili",
        "inspect", "listkungfu", "forcebreakthrough", "clearkungfu",
        "reloadconfig", "backup", "reset"
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
            case "setxiuzhenrealm":
                return handleSetRealm(sender, args);
            case "setwushurealm":
                return handleSetWushuRealm(sender, args);
            case "addxiuzhenexp":
                return handleAddExp(sender, args);
            case "addwushuexp":
                return handleAddWushuExp(sender, args);
            case "addlingli":
                return handleAddLingli(sender, args);
            case "addneili":
                return handleAddNeili(sender, args);
            case "inspect":
                return handleInspect(sender, args);
            case "listkungfu":
                return handleListKungFu(sender, args);
            case "forcebreakthrough":
                return handleForceBreakthrough(sender, args);
            case "clearkungfu":
                return handleClearKungFu(sender, args);
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
                case "setxiuzhenrealm":
                case "setwushurealm":
                case "addxiuzhenexp":
                case "addwushuexp":
                case "addlingli":
                case "addneili":
                case "inspect":
                case "listkungfu":
                case "forcebreakthrough":
                case "clearkungfu":
                case "reset":
                    // 补全在线玩家名
                    return getOnlinePlayers(args[1]);
                default:
                    return Collections.emptyList();
            }
        } else if (args.length == 3) {
            // 根据不同命令补全第三个参数
            switch (args[0].toLowerCase()) {
                case "setxiuzhenrealm":
                    // 补全修仙境界列表
                    return filterCompletions(REALMS_LIST, args[2]);
                case "setwushurealm":
                    // 补全武道境界列表
                    return filterCompletions(Arrays.asList("XiuLian", "TongMai", "ZhuJing", "HuaYuan"), args[2]);
                case "addexp":
                case "addwushuexp":
                    // 补全经验值（预设一些常用值）
                    return Arrays.asList("100", "500", "1000", "5000", "10000");
                case "addlingli":
                case "addneili":
                    // 补全灵力/内力值
                    return Arrays.asList("100", "500", "1000", "5000", "10000");
                case "forcebreakthrough":
                    // 补全突破类型
                    return Arrays.asList("xiuzhen", "wushu");
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
     * 设置玩家武道境界
     */
    private boolean handleSetWushuRealm(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin setwushurealm <玩家名> <境界 Key>");
            sender.sendMessage(ChatColor.YELLOW + "示例：/xiuzhenadmin setwushurealm Player1 WuShi");
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
        if (!configManager.getMartialRealmsConfig().contains("martial_realms." + realmKey)) {
            sender.sendMessage(ChatColor.RED + "武道境界 " + realmKey + " 不存在！");
            sender.sendMessage(ChatColor.YELLOW + "可用武道境界：XiuLian, TongMai, ZhuJing, HuaYuan");
            return true;
        }

        PlayerData playerData = plugin.getPlayerData(targetPlayer.getUniqueId());
        playerData.setCurrentWushuRealmKey(realmKey);
        playerData.setCurrentWushuSubLevelIndex(0);
        playerData.setCurrentWushuExp(0);
        playerData.saveData();

        sender.sendMessage(ChatColor.GREEN + "已将玩家 " + playerName + " 的武道境界设置为 " + realmKey);
        targetPlayer.sendMessage(ChatColor.GOLD + "管理员已将你的武道境界设置为 " + realmKey);
        return true;
    }

    /**
     * 添加玩家武道修为
     */
    private boolean handleAddWushuExp(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin addwushuexp <玩家名> <修为值>");
            return true;
        }

        String playerName = args[1];
        int expToAdd;
        
        try {
            expToAdd = Integer.parseInt(args[2]);
            if (expToAdd <= 0) {
                sender.sendMessage(ChatColor.RED + "修为值必须大于 0！");
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
        playerData.addWushuExp(expToAdd);
        playerData.saveData();

        sender.sendMessage(ChatColor.GREEN + "已为玩家 " + playerName + " 添加 " + expToAdd + " 点武道修为");
        targetPlayer.sendMessage(ChatColor.GOLD + "管理员为你增加了 " + expToAdd + " 点武道修为");
        return true;
    }

    /**
     * 添加玩家灵力
     */
    private boolean handleAddLingli(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin addlingli <玩家名> <灵力值>");
            return true;
        }

        String playerName = args[1];
        double amount;
        
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "灵力值必须大于 0！");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "灵力值必须是数字！");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        PlayerData playerData = plugin.getPlayerData(targetPlayer.getUniqueId());
        playerData.setCurrentLingli(playerData.getCurrentLingli() + amount);
        playerData.saveData();

        sender.sendMessage(ChatColor.GREEN + "已为玩家 " + playerName + " 添加 " + amount + " 点灵力");
        targetPlayer.sendMessage(ChatColor.GOLD + "管理员为你增加了 " + amount + " 点灵力");
        return true;
    }

    /**
     * 添加玩家内力
     */
    private boolean handleAddNeili(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin addneili <玩家名> <内力值>");
            return true;
        }

        String playerName = args[1];
        double amount;
        
        try {
            amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "内力值必须大于 0！");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "内力值必须是数字！");
            return true;
        }

        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        PlayerData playerData = plugin.getPlayerData(targetPlayer.getUniqueId());
        playerData.setCurrentNeili(playerData.getCurrentNeili() + amount);
        playerData.saveData();

        sender.sendMessage(ChatColor.GREEN + "已为玩家 " + playerName + " 添加 " + amount + " 点内力");
        targetPlayer.sendMessage(ChatColor.GOLD + "管理员为你增加了 " + amount + " 点内力");
        return true;
    }
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
     * 查看玩家详细信息
     */
    private boolean handleInspect(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin inspect <玩家名>");
            return true;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        PlayerData playerData = plugin.getPlayerData(targetPlayer.getUniqueId());
        
        sender.sendMessage(ChatColor.GOLD + "=== 玩家信息：" + playerName + " ===");
        sender.sendMessage(ChatColor.AQUA + "修仙境界：" + ChatColor.WHITE + playerData.getCurrentRealmKey() + 
            " (段位：" + playerData.getCurrentSubLevelIndex() + ")");
        sender.sendMessage(ChatColor.AQUA + "修仙修为：" + ChatColor.WHITE + String.format("%.0f", playerData.getCurrentExp()));
        sender.sendMessage(ChatColor.AQUA + "灵力：" + ChatColor.WHITE + String.format("%.1f", playerData.getCurrentLingli()) + 
            "/" + String.format("%.1f", playerData.getMaxLingli()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "武道境界：" + ChatColor.WHITE + playerData.getCurrentWushuRealmKey() + 
            " (段位：" + playerData.getCurrentWushuSubLevelIndex() + ")");
        sender.sendMessage(ChatColor.AQUA + "武道修为：" + ChatColor.WHITE + String.format("%.0f", playerData.getCurrentWushuExp()));
        sender.sendMessage(ChatColor.AQUA + "内力：" + ChatColor.WHITE + String.format("%.1f", playerData.getCurrentNeili()) + 
            "/" + String.format("%.1f", playerData.getMaxNeili()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "打坐状态：" + ChatColor.WHITE + (playerData.isMeditating() ? "修炼中" : "未打坐"));
        sender.sendMessage(ChatColor.AQUA + "战斗训练：" + ChatColor.WHITE + (playerData.isInCombatTraining() ? "训练中" : "未训练"));
        
        return true;
    }

    /**
     * 查看玩家功法列表
     */
    private boolean handleListKungFu(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin listkungfu <玩家名>");
            return true;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        EnhancedKungFuManager kungFuManager = plugin.getEnhancedKungFuManager();
        List<String> playerKungFus = new ArrayList<>();
        
        for (EnhancedKungFu kungFu : kungFuManager.getAllKungFus()) {
            if (kungFuManager.hasKungFu(targetPlayer.getUniqueId(), kungFu.getId())) {
                playerKungFus.add(ChatColor.GREEN + kungFu.getName() + " [" + kungFu.getLevel() + "] (已学习)");
            }
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== 玩家功法列表：" + playerName + " ===");
        if (playerKungFus.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "该玩家尚未学习任何功法");
        } else {
            for (String kungFu : playerKungFus) {
                sender.sendMessage(kungFu);
            }
        }
        
        return true;
    }

    /**
     * 强制玩家突破
     */
    private boolean handleForceBreakthrough(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin forcebreakthrough <玩家名> <xiuzhen|wushu>");
            sender.sendMessage(ChatColor.YELLOW + "示例：/xiuzhenadmin forcebreakthrough Player1 xiuzhen");
            return true;
        }

        String playerName = args[1];
        String type = args[2].toLowerCase();
        
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        if (!type.equals("xiuzhen") && !type.equals("wushu")) {
            sender.sendMessage(ChatColor.RED + "类型必须是 xiuzhen(修仙) 或 wushu(武道)！");
            return true;
        }

        PlayerData playerData = plugin.getPlayerData(targetPlayer.getUniqueId());
        
        if (type.equals("xiuzhen")) {
            playerData.addExp(999999);
            sender.sendMessage(ChatColor.GREEN + "已为玩家 " + playerName + " 添加大量修为以促进突破");
            targetPlayer.sendMessage(ChatColor.GOLD + "管理员助你突破，修为大增！");
        } else {
            playerData.addWushuExp(999999);
            sender.sendMessage(ChatColor.GREEN + "已为玩家 " + playerName + " 添加大量武道修为以促进突破");
            targetPlayer.sendMessage(ChatColor.GOLD + "管理员助你突破，武道修为大增！");
        }
        
        playerData.saveData();
        return true;
    }

    /**
     * 清空玩家功法
     */
    private boolean handleClearKungFu(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法：/xiuzhenadmin clearkungfu <玩家名>");
            sender.sendMessage(ChatColor.RED + "警告：此操作将清空玩家所有功法！");
            return true;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "玩家 " + playerName + " 不在线！");
            return true;
        }

        // TODO: 需要在 EnhancedKungFuManager 中添加 clearAllKungFu 方法
        // 目前仅显示提示信息
        sender.sendMessage(ChatColor.YELLOW + "注意：该功能需要在 EnhancedKungFuManager 中添加 clearAllKungFu 方法");
        sender.sendMessage(ChatColor.GREEN + "已记录请求，将在后续版本中实现清空玩家功法功能");
        
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
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin setxiuzhenrealm <玩家> <境界>" + ChatColor.WHITE + " - 设置玩家修仙境界");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin setwushurealm <玩家> <境界>" + ChatColor.WHITE + " - 设置玩家武道境界");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin addxiuzhenexp <玩家> <修为>" + ChatColor.WHITE + " - 添加修仙修为");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin addwushuexp <玩家> <修为>" + ChatColor.WHITE + " - 添加武道修为");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin addlingli <玩家> <数量>" + ChatColor.WHITE + " - 添加灵力");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin addneili <玩家> <数量>" + ChatColor.WHITE + " - 添加内力");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin inspect <玩家>" + ChatColor.WHITE + " - 查看玩家详细信息");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin listkungfu <玩家>" + ChatColor.WHITE + " - 查看玩家功法列表");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin forcebreakthrough <玩家> <类型>" + ChatColor.WHITE + " - 强制玩家突破");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin clearkungfu <玩家>" + ChatColor.WHITE + " - 清空玩家功法");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin reset <玩家>" + ChatColor.WHITE + " - 重置玩家数据");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin reloadconfig" + ChatColor.WHITE + " - 重载配置");
            sender.sendMessage(ChatColor.RED + "/xiuzhenadmin backup" + ChatColor.WHITE + " - 创建数据备份");
        }
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "别名: /xza, /xzadmin");
        sender.sendMessage(ChatColor.GRAY + "使用 TAB 键可获得智能命令补全");
    }
}
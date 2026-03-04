package com.adlamb.simplexiuzhen.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.adlamb.simplexiuzhen.SimpleXiuzhen;

/**
 * 命令基类 - 提供通用的命令处理功能
 * 减少命令处理器中的重复代码
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    protected final SimpleXiuzhen plugin;
    protected final List<String> mainCommands;
    
    /**
     * 构造函数
     * @param plugin 插件主类实例
     * @param mainCommands 主命令列表
     */
    public BaseCommand(SimpleXiuzhen plugin, List<String> mainCommands) {
        this.plugin = plugin;
        this.mainCommands = mainCommands != null ? new ArrayList<>(mainCommands) : new ArrayList<>();
    }
    
    /**
     * TAB 补全逻辑
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 补全主命令
            return filterCompletions(mainCommands, args[0]);
        }
        
        // 子类可以扩展更复杂的补全逻辑
        return onTabCompleteExtended(sender, command, alias, args);
    }
    
    /**
     * 过滤补全选项（前缀匹配）
     * @param options 可选列表
     * @param input 输入内容
     * @return 匹配的选项列表
     */
    protected List<String> filterCompletions(List<String> options, String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>(options);
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
     * 扩展的 TAB 补全方法
     * 子类可以重写此方法提供更复杂的补全逻辑
     * @param sender 命令发送者
     * @param command 命令对象
     * @param alias 命令别名
     * @param args 命令参数
     * @return 补全列表
     */
    protected List<String> onTabCompleteExtended(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
    
    /**
     * 检查发送者是否有权限
     * @param sender 命令发送者
     * @param permission 权限节点
     * @return 是否有权限
     */
    protected boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }
    
    /**
     * 检查发送者是否是玩家
     * @param sender 命令发送者
     * @return 是否是玩家
     */
    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof org.bukkit.entity.Player;
    }
}

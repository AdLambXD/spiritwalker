package com.adlamb.simplexiuzhen.utils;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * 消息工具类，简化 MiniMessage 的使用
 */
public class MessageUtils {
    
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    /**
     * 发送带颜色的消息
     * @param sender 接收者
     * @param message 消息（支持 MiniMessage 格式，如 <red>红色文本</red>）
     */
    public static void send(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(MINI_MESSAGE.deserialize(message));
        }
    }
    
    /**
     * 发送带颜色的消息（带变量替换）
     * @param sender 接收者
     * @param message 消息模板
     * @param args 替换参数
     */
    public static void send(CommandSender sender, String message, Object... args) {
        if (sender != null) {
            String formatted = String.format(message, args);
            sender.sendMessage(MINI_MESSAGE.deserialize(formatted));
        }
    }
}

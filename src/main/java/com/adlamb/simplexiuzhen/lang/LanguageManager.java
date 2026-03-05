package com.adlamb.simplexiuzhen.lang;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 语言管理器
 * 负责处理插件的多语言支持
 */
public class LanguageManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private YamlConfiguration languageConfig;
    private String currentLanguage;
    
    // 默认语言键值映射
    private static final Map<String, String> DEFAULT_MESSAGES = new HashMap<>();
    static {
        // 基础消息
        DEFAULT_MESSAGES.put("messages.player_only", "&c只有玩家可以使用此命令！");
        DEFAULT_MESSAGES.put("messages.unknown_subcommand", "&c未知的子命令！使用 /xiuzhen 查看帮助");
        DEFAULT_MESSAGES.put("messages.no_permission", "&c你没有权限使用此命令！");
        
        // 打坐相关
        DEFAULT_MESSAGES.put("messages.meditate_start", "&a开始打坐修炼！");
        DEFAULT_MESSAGES.put("messages.meditate_start_tip", "&e保持静止以获得修为...");
        DEFAULT_MESSAGES.put("messages.meditate_stop", "&c停止打坐修炼。");
        DEFAULT_MESSAGES.put("messages.meditate_interrupt", "&c你移动了，打坐被打断！");
        DEFAULT_MESSAGES.put("messages.meditate_actionbar", "&a打坐修炼中... (+{gain} 修为)");
        DEFAULT_MESSAGES.put("messages.meditate_sitting_actionbar", "&a坐着修炼中... (+{gain} 修为)");
        DEFAULT_MESSAGES.put("messages.meditate_riding_actionbar", "&a骑乘冥想中... (+{gain} 修为)");
        
        // 状态显示
        DEFAULT_MESSAGES.put("messages.stats_title", "&6=== 修仙详细状态 ===");
        DEFAULT_MESSAGES.put("messages.stats_realm", "&b境界: &f{realm}{sublevel}");
        DEFAULT_MESSAGES.put("messages.stats_exp", "&b修为: &f{current}/{required}");
        DEFAULT_MESSAGES.put("messages.stats_exp_max", "&b修为: &f{current} (&a已满级&f)");
        DEFAULT_MESSAGES.put("messages.stats_status_meditating", "&b状态: &a打坐中");
        DEFAULT_MESSAGES.put("messages.stats_status_not_meditating", "&b状态: &c未打坐");
        DEFAULT_MESSAGES.put("messages.stats_attribute_bonus", "&d属性加成:");
        DEFAULT_MESSAGES.put("messages.stats_health_bonus", "  &f+ {amount} 最大生命值");
        DEFAULT_MESSAGES.put("messages.stats_mana_bonus", "  &f+ {amount} 最大灵力");
        
        // 境界信息
        DEFAULT_MESSAGES.put("messages.realm_info_title", "&6=== 境界体系介绍 ===");
        DEFAULT_MESSAGES.put("messages.realm_info_entry", "&b{realm} (等级 {level})");
        DEFAULT_MESSAGES.put("messages.realm_info_sublevel", "  &f- {name}: 需要修为 {exp}");
        DEFAULT_MESSAGES.put("messages.realm_info_sublevel_failed", "  &c段位信息读取失败");
        DEFAULT_MESSAGES.put("messages.realm_info_tip", "&e使用 /xiuzhen stats 查看个人状态");
        
        // 排行榜
        DEFAULT_MESSAGES.put("messages.ranking_title", "&6=== 修仙排行榜 (第{page}页) ===");
        DEFAULT_MESSAGES.put("messages.ranking_entry", "&a{rank}. &f{player} &7- &b{realm}{sublevel} &7(&f{exp}修为&7)");
        DEFAULT_MESSAGES.put("messages.ranking_empty", "&c暂无数据");
        DEFAULT_MESSAGES.put("messages.ranking_page_invalid", "&c页码必须是正整数！");
        
        // 帮助信息
        DEFAULT_MESSAGES.put("messages.help_title", "&6=== 修仙系统命令帮助 ===");
        DEFAULT_MESSAGES.put("messages.help_player_base", "&b/xiuzhen &f- 查看基本信息");
        DEFAULT_MESSAGES.put("messages.help_player_meditate", "&b/xiuzhen meditate &f- 开始/停止打坐");
        DEFAULT_MESSAGES.put("messages.help_player_stats", "&b/xiuzhen stats &f- 查看详细状态");
        DEFAULT_MESSAGES.put("messages.help_player_top", "&b/xiuzhen top [页码] &f- 查看排行榜");
        DEFAULT_MESSAGES.put("messages.help_player_info", "&b/xiuzhen info &f- 查看境界介绍");
        DEFAULT_MESSAGES.put("messages.help_admin_title", "&c管理员命令:");
        DEFAULT_MESSAGES.put("messages.help_admin_setrealm", "&c/xiuzhenadmin setrealm <玩家> <境界> &f- 设置玩家境界");
        DEFAULT_MESSAGES.put("messages.help_admin_addexp", "&c/xiuzhenadmin addexp <玩家> <修为> &f- 添加修为");
        DEFAULT_MESSAGES.put("messages.help_admin_reload", "&c/xiuzhenadmin reloadconfig &f- 重载配置");
        DEFAULT_MESSAGES.put("messages.help_admin_backup", "&c/xiuzhenadmin backup &f- 创建数据备份");
        DEFAULT_MESSAGES.put("messages.help_admin_reset", "&c/xiuzhenadmin reset <玩家> &f- 重置玩家数据");
        DEFAULT_MESSAGES.put("messages.help_aliases", "&7别名: /xz, /xiuxian");
        DEFAULT_MESSAGES.put("messages.help_admin_aliases", "&7别名: /xza, /xzadmin");
        DEFAULT_MESSAGES.put("messages.help_tab_complete", "&7使用 TAB 键可获得智能命令补全");
        
        // 错误消息
        DEFAULT_MESSAGES.put("messages.error_page_number", "&c页码必须是数字！");
        DEFAULT_MESSAGES.put("messages.error_player_not_found", "&c玩家 {player} 不在线！");
        DEFAULT_MESSAGES.put("messages.error_player_never_joined", "&c玩家 {player} 从未加入过服务器！");
        DEFAULT_MESSAGES.put("messages.error_invalid_realm", "&c境界 {realm} 不存在！");
        DEFAULT_MESSAGES.put("messages.error_invalid_exp", "&c修为值必须是大于0的数字！");
        DEFAULT_MESSAGES.put("messages.error_config_reload", "&c配置重载失败: {error}");
        DEFAULT_MESSAGES.put("messages.error_data_reset", "&c数据重置失败: {error}");
        DEFAULT_MESSAGES.put("messages.error_backup", "&c备份失败: {error}");
        
        // 成功消息
        DEFAULT_MESSAGES.put("messages.success_config_reload", "&a配置文件重载成功！");
        DEFAULT_MESSAGES.put("messages.success_config_reload_detail", "&e当前存储类型: {storage}");
        DEFAULT_MESSAGES.put("messages.success_data_reset", "&a已重置玩家 {player} 的所有修仙数据");
        DEFAULT_MESSAGES.put("messages.success_backup", "&a数据备份完成: {backup}");
        DEFAULT_MESSAGES.put("messages.success_backup_location", "&e备份位置: {location}");
        DEFAULT_MESSAGES.put("messages.success_realm_set", "&a已将玩家 {player} 的境界设置为 {realm}");
        DEFAULT_MESSAGES.put("messages.success_exp_added", "&a已为玩家 {player} 添加 {exp} 点修为");
        
        // 管理员通知
        DEFAULT_MESSAGES.put("messages.admin_realm_set_target", "&6管理员已将你的境界设置为 {realm}");
        DEFAULT_MESSAGES.put("messages.admin_exp_added_target", "&6管理员为你增加了 {exp} 点修为");
        DEFAULT_MESSAGES.put("messages.admin_data_reset_target", "&c你的修仙数据已被管理员重置");
        
        // 冥想相关
        DEFAULT_MESSAGES.put("messages.meditation_start_riding", "&a你骑乘着冥想盔甲架，自动进入打坐状态！");
        DEFAULT_MESSAGES.put("messages.meditation_start_riding_tip", "&e保持静止以获得修为加成...");
        DEFAULT_MESSAGES.put("messages.meditation_stop_riding", "&c你离开了冥想盔甲架，打坐状态结束。");
        DEFAULT_MESSAGES.put("messages.meditation_interrupt_move", "&c移动中断了打坐修炼！");
        
        // 怪物击杀
        DEFAULT_MESSAGES.put("messages.mob_kill_exp_gain", "&a击杀{mob}获得 {exp} 点修为！");
        
        // 战斗训练相关
        DEFAULT_MESSAGES.put("messages.combat_start", "&a开始战斗训练！");
        DEFAULT_MESSAGES.put("messages.combat_start_tip", "&e通过战斗来提升武者修为和内力...");
        DEFAULT_MESSAGES.put("messages.combat_stop", "&c停止战斗训练。");
        DEFAULT_MESSAGES.put("messages.combat_training_actionbar", "&c战斗训练中... (+{gain} 武者修为)");
        DEFAULT_MESSAGES.put("messages.training_actionbar", "&e训练中... (+{gain} 武者修为)");
        DEFAULT_MESSAGES.put("messages.combat_interrupt", "&c长时间未战斗，训练效果减弱！");
        
        // 双系统状态
        DEFAULT_MESSAGES.put("messages.dual_system_status", "&6=== 双系统状态 ===");
        DEFAULT_MESSAGES.put("messages.xiuzhen_status", "&b修仙: &f境界 {realm}{sublevel} 修为 {exp} 灵力 {lingli}/{max_lingli}");
        DEFAULT_MESSAGES.put("messages.wushu_status", "&c武者: &f境界 {realm}{sublevel} 修为 {exp} 内力 {neili}/{max_neili}");
        DEFAULT_MESSAGES.put("messages.meditation_status", "&a打坐状态: {status}");
        DEFAULT_MESSAGES.put("messages.training_status", "&c训练状态: {status}");
        DEFAULT_MESSAGES.put("messages.interactive_stop_meditate", "&6[点击停止打坐]");
        DEFAULT_MESSAGES.put("messages.interactive_stop_meditate_hover", "点击立即停止打坐");
        DEFAULT_MESSAGES.put("messages.interactive_start_meditate", "&a[开始打坐]");
        DEFAULT_MESSAGES.put("messages.interactive_start_meditate_hover", "开始修炼获得修为");
        DEFAULT_MESSAGES.put("messages.interactive_view_rankings", "&b[查看排行]");
        DEFAULT_MESSAGES.put("messages.interactive_view_rankings_hover", "查看服务器修仙排行榜");
        DEFAULT_MESSAGES.put("messages.interactive_menu_title", "&7--- 快捷操作 ---");
        
        // 系统消息
        DEFAULT_MESSAGES.put("system.plugin_enabled", "SimpleXiuzhen 插件已启用！");
        DEFAULT_MESSAGES.put("system.plugin_disabled", "SimpleXiuzhen 插件已禁用！");
        DEFAULT_MESSAGES.put("system.data_saved", "自动保存所有玩家数据完成");
        DEFAULT_MESSAGES.put("system.compatible_plugins", "兼容插件: {plugins}");
    }

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.currentLanguage = "zh_cn"; // 默认中文
        loadLanguage();
    }

    /**
     * 加载语言文件
     */
    public void loadLanguage() {
        String langFileName = "lang/" + currentLanguage + ".yml";
        File langFile = new File(plugin.getDataFolder(), langFileName);
        
        try {
            // 如果语言文件不存在，从资源中复制
            if (!langFile.exists()) {
                plugin.saveResource(langFileName, false);
            }
            
            languageConfig = YamlConfiguration.loadConfiguration(langFile);
            
            // 确保所有默认消息都有对应的翻译
            for (Map.Entry<String, String> entry : DEFAULT_MESSAGES.entrySet()) {
                if (!languageConfig.contains(entry.getKey())) {
                    languageConfig.set(entry.getKey(), entry.getValue());
                }
            }
            
            // 保存更新后的配置
            languageConfig.save(langFile);
            
            logger.info("语言文件加载成功: " + currentLanguage);
            
        } catch (IOException e) {
            logger.severe("加载语言文件失败: " + e.getMessage());
            // 使用默认消息
            languageConfig = new YamlConfiguration();
            for (Map.Entry<String, String> entry : DEFAULT_MESSAGES.entrySet()) {
                languageConfig.set(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 获取本地化消息
     * @param key 消息键
     * @param params 参数替换
     * @return 格式化后的消息
     */
    public String getMessage(String key, Object... params) {
        String message = languageConfig.getString(key, DEFAULT_MESSAGES.getOrDefault(key, key));
        
        // 参数替换
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                String placeholder = "{" + params[i] + "}";
                String value = String.valueOf(params[i + 1]);
                message = message.replace(placeholder, value);
            }
        }
        
        // 颜色代码转换 - 使用 Legacy 格式支持
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(
            net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(
                message.replace('&', '§')
            )
        );
    }

    /**
     * 获取系统消息
     */
    public String getSystemMessage(String key, Object... params) {
        return getMessage("system." + key, params);
    }

    /**
     * 获取玩家消息
     */
    public String getPlayerMessage(String key, Object... params) {
        return getMessage("messages." + key, params);
    }

    /**
     * 设置当前语言
     */
    public void setLanguage(String language) {
        this.currentLanguage = language;
        loadLanguage();
    }

    /**
     * 获取当前语言
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * 重新加载语言配置
     */
    public void reload() {
        loadLanguage();
    }
}
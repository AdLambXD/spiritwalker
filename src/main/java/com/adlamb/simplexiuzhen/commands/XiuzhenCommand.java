package com.adlamb.simplexiuzhen.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.adlamb.simplexiuzhen.BreakthroughSystem;
import com.adlamb.simplexiuzhen.ConfigManager;
import com.adlamb.simplexiuzhen.EnhancedKungFu;
import com.adlamb.simplexiuzhen.EnhancedKungFuManager;
import com.adlamb.simplexiuzhen.PlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.ranking.RankingManager;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * 玩家命令处理器 - 统一处理修仙和武者系统
 * 所有修炼相关命令都使用 /xz 或 /xiuzhen
 */
public class XiuzhenCommand extends BaseCommand {
    private final ConfigManager configManager;
    private final RankingManager rankingManager;
    private final EnhancedKungFuManager kungFuManager;
    private final BreakthroughSystem breakthroughSystem;
    
    // 主命令列表（包含修仙和武者）
    private static final List<String> MAIN_COMMANDS = Arrays.asList(
        "meditate", "stats", "top", "info", "gui", "train", "breakthrough", "kungfu"
    );
    
    // 数字参数（用于分页）
    private static final List<String> NUMBER_ARGS = Arrays.asList("1", "2", "3", "4", "5");
    
    // 武者功法子命令
    private static final List<String> KUNGFU_SUBCOMMANDS = Arrays.asList(
        "list", "learn", "use", "info"
    );

    public XiuzhenCommand(SimpleXiuzhen plugin) {
        super(plugin, MAIN_COMMANDS);
        this.configManager = plugin.getConfigManager();
        this.rankingManager = new RankingManager(plugin);
        this.kungFuManager = plugin.getEnhancedKungFuManager();
        this.breakthroughSystem = plugin.getBreakthroughSystem();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isPlayer(sender)) {
            sender.sendMessage(Component.text("只有玩家可以使用此命令！", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length == 0) {
            // 显示帮助信息
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "meditate":
                return handleMeditate(player, args);
            case "stats":
            case "zt":
                return handleStats(player);
            case "top":
                return handleTop(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "gui":
                return handleGui(player);
            case "train":
                return handleTrain(player);
            case "breakthrough":
                return handleBreakthrough(player);
            case "kungfu":
                return handleKungFu(player, args);
            default:
                player.sendMessage(Component.text("未知的子命令！使用 /xz 查看帮助", NamedTextColor.RED));
                return true;
        }
    }
    
    @Override
    protected List<String> onTabCompleteExtended(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2 && "kungfu".equalsIgnoreCase(args[0])) {
            return filterCompletions(KUNGFU_SUBCOMMANDS, args[1]);
        } else if (args.length == 3 && "kungfu".equalsIgnoreCase(args[0])) {
            if ("learn".equalsIgnoreCase(args[1]) || "use".equalsIgnoreCase(args[1]) || "info".equalsIgnoreCase(args[1])) {
                return Arrays.asList("fire_ball", "lightning_strike", "heal");
            }
        } else if (args.length == 2 && "top".equalsIgnoreCase(args[0])) {
            return filterCompletions(NUMBER_ARGS, args[1]);
        }
        return Collections.emptyList();
    }

    /**
     * 处理打坐命令（修仙）
     */
    private boolean handleMeditate(Player player, String[] args) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        playerData.toggleMeditation(player);

        if (playerData.isMeditating()) {
            player.sendMessage(Component.text("开始打坐修炼！", NamedTextColor.GREEN));
            player.sendMessage(Component.text("保持静止以获得修为...", NamedTextColor.YELLOW));
            
            TextComponent component = new TextComponent(NamedTextColor.GOLD + "[点击停止打坐]");
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xz meditate"));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                new ComponentBuilder("点击立即停止打坐").create()));
            player.spigot().sendMessage(component);
        } else {
            player.sendMessage(Component.text("停止打坐修炼。", NamedTextColor.RED));
        }
        return true;
    }
    
    /**
     * 处理状态查看命令（统一显示修仙和武者）
     */
    private boolean handleStats(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        displayUnifiedStats(player, playerData);
        return true;
    }
    
    /**
     * 处理战斗训练命令（武者）
     */
    private boolean handleTrain(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        playerData.toggleCombatTraining(player);
        
        if (playerData.isInCombatTraining()) {
            player.sendMessage(Component.text("开始武者战斗训练！", NamedTextColor.GREEN));
            player.sendMessage(Component.text("通过战斗来提升内力和武者修为...", NamedTextColor.YELLOW));
            
            player.sendMessage(NamedTextColor.AQUA + "当前内力：" + NamedTextColor.WHITE + 
                String.format("%.1f", playerData.getCurrentNeili()) + "/" + 
                String.format("%.1f", playerData.getMaxNeili()));
            player.sendMessage(NamedTextColor.AQUA + "武者境界：" + NamedTextColor.WHITE + 
                getWushuRealmDisplay(playerData));
        } else {
            player.sendMessage(Component.text("停止武者战斗训练。", NamedTextColor.RED));
        }
        
        return true;
    }
    
    /**
     * 处理突破命令（武者）
     */
    private boolean handleBreakthrough(Player player) {
        PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
        if (breakthroughSystem.attemptWushuBreakthrough(player, playerData)) {
            player.sendMessage(Component.text("武道突破成功！", NamedTextColor.GOLD));
            displayUnifiedStats(player, playerData);
        } else {
            player.sendMessage(Component.text("突破条件不满足或突破失败！", NamedTextColor.RED));
        }
        return true;
    }
    
    /**
     * 处理功法命令
     */
    private boolean handleKungFu(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("请指定功法操作：list, learn, use, info", NamedTextColor.RED));
            return true;
        }
        
        switch (args[1].toLowerCase()) {
            case "list":
                listKungFus(player);
                break;
            case "learn":
                if (args.length >= 3) {
                    learnKungFu(player, args[2]);
                } else {
                    player.sendMessage(Component.text("请指定功法 ID", NamedTextColor.RED));
                }
                break;
            case "use":
                if (args.length >= 3) {
                    useKungFu(player, args[2]);
                } else {
                    player.sendMessage(Component.text("请指定功法 ID", NamedTextColor.RED));
                }
                break;
            case "info":
                if (args.length >= 3) {
                    showKungFuInfo(player, args[2]);
                } else {
                    player.sendMessage(Component.text("请指定功法 ID", NamedTextColor.RED));
                }
                break;
            default:
                player.sendMessage(Component.text("未知的功法操作！使用：list, learn, use, info", NamedTextColor.RED));
        }
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
                sender.sendMessage(Component.text("页码必须是数字！", NamedTextColor.RED));
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
        
        sender.sendMessage(Component.text("=== 境界体系介绍 ===", NamedTextColor.GOLD));
        
        // 获取所有境界
        Set<String> realms = realmsConfig.getConfigurationSection("realms").getKeys(false);
        for (String realmKey : realms) {
            String displayName = realmsConfig.getString("realms." + realmKey + ".display_name", realmKey);
            int level = realmsConfig.getInt("realms." + realmKey + ".level", 0);
            
            sender.sendMessage(NamedTextColor.AQUA + displayName + " (等级 " + level + ")");
            
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
                    sender.sendMessage("  " + NamedTextColor.WHITE + "- " + name + ": 需要修为 " + requiredExp);
                }
            } catch (Exception e) {
                sender.sendMessage("  " + NamedTextColor.RED + "段位信息读取失败");
            }
            sender.sendMessage("");
        }
        
        sender.sendMessage(Component.text("使用 /xz stats 查看个人状态", NamedTextColor.YELLOW));
        return true;
    }
    
    /**
     * 处理 GUI 命令
     */
    private boolean handleGui(Player player) {
        plugin.getGuiManager().openMainGui(player);
        return true;
    }
    
    /**
     * 显示统一的状态信息（新模板）
     */
    private void displayUnifiedStats(Player player, PlayerData playerData) {
        player.sendMessage(Component.text("---==修炼状态==---", NamedTextColor.GOLD));
        
        // 修仙境界
        String xiuzhenRealm = getXiuzhenRealmDisplay(playerData);
        String xiuzhenNext = getNextXiuzhenRequirement(playerData);
        player.sendMessage(NamedTextColor.AQUA + "修仙境界：" + NamedTextColor.WHITE + xiuzhenRealm);
        
        // 武者境界
        String wushuRealm = getWushuRealmDisplay(playerData);
        String wushuNext = getNextWushuRequirement(playerData);
        player.sendMessage(NamedTextColor.AQUA + "武者境界：" + NamedTextColor.WHITE + wushuRealm);
        
        player.sendMessage(Component.text("-----------------------", NamedTextColor.GOLD));
        
        // 打坐状态
        String meditationStatus = playerData.isMeditating() ? 
            NamedTextColor.GREEN + "打坐中..." : NamedTextColor.RED + "未打坐";
        player.sendMessage(NamedTextColor.AQUA + "打坐状态：" + meditationStatus);
        
        player.sendMessage(Component.text("---==========---", NamedTextColor.GOLD));
    }
    
    /**
     * 获取修仙境界显示字符串
     */
    private String getXiuzhenRealmDisplay(PlayerData data) {
        String realmKey = data.getCurrentRealmKey();
        int index = data.getCurrentSubLevelIndex();
        double exp = data.getCurrentExp();
        double nextExp = getNextXiuzhenExp(data);
        
        String realmName = getRealmName(realmKey, true);
        String subLevelName = getSubLevelName(realmKey, index, true);
        
        if (nextExp <= 0) {
            return realmName + subLevelName + " (已满级)";
        }
        return realmName + subLevelName + " - (" + String.format("%.0f", exp) + "/" + String.format("%.0f", nextExp) + ")";
    }
    
    /**
     * 获取武者境界显示字符串
     */
    private String getWushuRealmDisplay(PlayerData data) {
        String realmKey = data.getCurrentWushuRealmKey();
        int index = data.getCurrentWushuSubLevelIndex();
        double exp = data.getCurrentWushuExp();
        double nextExp = getNextWushuExp(data);
        
        String realmName = getRealmName(realmKey, false);
        String subLevelName = getSubLevelName(realmKey, index, false);
        
        if (nextExp <= 0) {
            return realmName + subLevelName + " (已满级)";
        }
        return realmName + subLevelName + " - (" + String.format("%.0f", exp) + "/" + String.format("%.0f", nextExp) + ")";
    }
    
    /**
     * 获取下一层境界要求
     */
    private String getNextXiuzhenRequirement(PlayerData data) {
        double nextExp = getNextXiuzhenExp(data);
        if (nextExp <= 0) {
            return "已达当前境界巅峰";
        }
        return "还需修为：" + String.format("%.0f", nextExp - data.getCurrentExp());
    }
    
    private String getNextWushuRequirement(PlayerData data) {
        double nextExp = getNextWushuExp(data);
        if (nextExp <= 0) {
            return "已达当前境界巅峰";
        }
        return "还需修为：" + String.format("%.0f", nextExp - data.getCurrentWushuExp());
    }
    
    /**
     * 获取下一境界所需修为
     */
    private double getNextXiuzhenExp(PlayerData data) {
        try {
            FileConfiguration config = configManager.getRealmsConfig();
            List<?> subLevels = config.getList("realms." + data.getCurrentRealmKey() + ".sub_levels");
            if (subLevels == null) return -1;
            
            int nextIndex = data.getCurrentSubLevelIndex() + 1;
            if (nextIndex >= subLevels.size()) return -1;
            
            Map<?, ?> subLevel = (Map<?, ?>) subLevels.get(nextIndex);
            Object expObj = subLevel.get("required_exp");
            return (expObj instanceof Number) ? ((Number) expObj).doubleValue() : -1;
        } catch (Exception e) {
            return -1;
        }
    }
    
    private double getNextWushuExp(PlayerData data) {
        try {
            FileConfiguration config = configManager.getMartialRealmsConfig();
            List<?> subLevels = config.getList("martial_realms." + data.getCurrentWushuRealmKey() + ".sub_levels");
            if (subLevels == null) return -1;
            
            int nextIndex = data.getCurrentWushuSubLevelIndex() + 1;
            if (nextIndex >= subLevels.size()) return -1;
            
            Map<?, ?> subLevel = (Map<?, ?>) subLevels.get(nextIndex);
            Object expObj = subLevel.get("required_exp");
            return (expObj instanceof Number) ? ((Number) expObj).doubleValue() : -1;
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 获取境界名称
     */
    private String getRealmName(String realmKey, boolean isXiuzhen) {
        try {
            FileConfiguration config = isXiuzhen ? configManager.getRealmsConfig() : configManager.getMartialRealmsConfig();
            String path = isXiuzhen ? "realms." + realmKey + ".display_name" : "martial_realms." + realmKey + ".display_name";
            return config.getString(path, realmKey);
        } catch (Exception e) {
            return realmKey;
        }
    }
    
    /**
     * 获取段位名称
     */
    private String getSubLevelName(String realmKey, int index, boolean isXiuzhen) {
        try {
            FileConfiguration config = isXiuzhen ? configManager.getRealmsConfig() : configManager.getMartialRealmsConfig();
            String path = isXiuzhen ? "realms." + realmKey + ".sub_levels." + index + ".name" : "martial_realms." + realmKey + ".sub_levels." + index + ".name";
            return config.getString(path, "未知段位");
        } catch (Exception e) {
            return "未知段位";
        }
    }
    
    /**
     * 显示功法列表
     */
    private void listKungFus(Player player) {
        List<EnhancedKungFu> allKungFus = new ArrayList<>(kungFuManager.getAllKungFus());
        
        player.sendMessage(Component.text("=== 功法列表 ===", NamedTextColor.GOLD));
        for (EnhancedKungFu kungFu : allKungFus) {
            String status = kungFuManager.hasKungFu(player.getUniqueId(), kungFu.getId()) ? 
                NamedTextColor.GREEN + "[已学习]" : 
                (kungFuManager.canLearnKungFu(player.getUniqueId(), kungFu.getId()) ? 
                    NamedTextColor.YELLOW + "[可学习]" : 
                    NamedTextColor.GRAY + "[未解锁]");
            
            player.sendMessage(status + " " + NamedTextColor.WHITE + kungFu.getName() + 
                " (" + kungFu.getTypeDisplay() + ") - " + kungFu.getDescription());
        }
    }
    
    /**
     * 学习功法
     */
    private void learnKungFu(Player player, String kungFuId) {
        if (kungFuManager.learnKungFu(player.getUniqueId(), kungFuId)) {
            EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
            player.sendMessage(NamedTextColor.GREEN + "成功学习功法：" + kungFu.getName());
        } else {
            player.sendMessage(Component.text("无法学习该功法，请检查境界和修为要求", NamedTextColor.RED));
        }
    }
    
    /**
     * 使用功法
     */
    private void useKungFu(Player player, String kungFuId) {
        EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
        if (kungFu == null) {
            player.sendMessage(Component.text("找不到该功法！", NamedTextColor.RED));
            return;
        }

        if (kungFuManager.useKungFu(player.getUniqueId(), kungFuId)) {
            player.sendMessage(NamedTextColor.GREEN + "施展功法：" + kungFu.getName());
            player.sendMessage(NamedTextColor.YELLOW + "效果：" + String.join(", ", kungFu.getEffects()));
        } else {
            player.sendMessage(Component.text("功法使用失败！", NamedTextColor.RED));
        }
    }
    
    /**
     * 显示功法信息
     */
    private void showKungFuInfo(Player player, String kungFuId) {
        EnhancedKungFu kungFu = kungFuManager.getKungFu(kungFuId);
        if (kungFu == null) {
            player.sendMessage(Component.text("找不到该功法！", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("=== 功法详情 ===", NamedTextColor.GOLD));
        player.sendMessage(NamedTextColor.AQUA + "名称：" + NamedTextColor.WHITE + kungFu.getName());
        player.sendMessage(NamedTextColor.AQUA + "类型：" + NamedTextColor.WHITE + kungFu.getTypeDisplay());
        player.sendMessage(NamedTextColor.AQUA + "等级：" + NamedTextColor.WHITE + kungFu.getLevelDisplay());
        player.sendMessage(NamedTextColor.AQUA + "灵力消耗：" + NamedTextColor.WHITE + kungFu.getLingliCost());
        player.sendMessage(NamedTextColor.AQUA + "内力消耗：" + NamedTextColor.WHITE + kungFu.getNeiliCost());
        player.sendMessage(NamedTextColor.AQUA + "冷却时间：" + NamedTextColor.WHITE + (kungFu.getCooldownTicks() / 20) + "秒");
        player.sendMessage(NamedTextColor.AQUA + "描述：" + NamedTextColor.WHITE + kungFu.getDescription());
    }

    /**
     * 显示帮助信息
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== 修仙系统命令帮助 ===", NamedTextColor.GOLD));
        sender.sendMessage(NamedTextColor.AQUA + "/xz" + NamedTextColor.WHITE + " - 查看基本信息");
        sender.sendMessage(NamedTextColor.AQUA + "/xz meditate" + NamedTextColor.WHITE + " - 开始/停止打坐");
        sender.sendMessage(NamedTextColor.AQUA + "/xz stats" + NamedTextColor.WHITE + " - 查看详细状态");
        sender.sendMessage(NamedTextColor.AQUA + "/xz top [页码]" + NamedTextColor.WHITE + " - 查看排行榜");
        sender.sendMessage(NamedTextColor.AQUA + "/xz info" + NamedTextColor.WHITE + " - 查看境界介绍");
        sender.sendMessage(NamedTextColor.AQUA + "/xz gui" + NamedTextColor.WHITE + " - 打开图形界面");
        sender.sendMessage("");
        sender.sendMessage(Component.text("提示：别名 /xiuzhen, /xz /xx 均可使用", NamedTextColor.GRAY));
    }
}

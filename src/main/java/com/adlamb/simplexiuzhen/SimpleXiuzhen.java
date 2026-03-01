package com.adlamb.simplexiuzhen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.adlamb.simplexiuzhen.commands.XiuzhenAdminCommand;
import com.adlamb.simplexiuzhen.commands.XiuzhenCommand;
import com.adlamb.simplexiuzhen.database.DatabaseManager;
import com.adlamb.simplexiuzhen.database.PlayerDataDAO;
import com.adlamb.simplexiuzhen.integration.ThirdPartyIntegration;
import com.adlamb.simplexiuzhen.lang.LanguageManager;
import com.adlamb.simplexiuzhen.listeners.MobKillListener;
import com.adlamb.simplexiuzhen.listeners.RideMeditationListener;
import com.adlamb.simplexiuzhen.permissions.XiuzhenPermissions;
import com.adlamb.simplexiuzhen.placeholder.XiuzhenPlaceholderExpansion;



/**
 * 主类 - 修仙系统插件
 */
public class SimpleXiuzhen extends JavaPlugin implements CommandExecutor, Listener {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerDataDAO playerDataDAO;
    private RideMeditationListener rideMeditationListener;
    private MobKillListener mobKillListener;
    private ThirdPartyIntegration thirdPartyIntegration;
    private XiuzhenPermissions permissionsManager;
    private LanguageManager languageManager;
    private Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private BukkitTask cultivationTask;
    private BukkitTask autoSaveTask;

    @Override
    public void onEnable() {
        getLogger().info("SimpleXiuzhen 插件正在启用...");

        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化数据库管理器
        databaseManager = new DatabaseManager(this);
        playerDataDAO = new PlayerDataDAO(this, databaseManager);
        
        // 初始化权限管理器
        permissionsManager = new XiuzhenPermissions(this);
        
        // 初始化语言管理器
        languageManager = new LanguageManager(this);
        
        // 初始化骑乘冥想监听器
        rideMeditationListener = new RideMeditationListener(this);
        
        // 初始化怪物击杀监听器
        mobKillListener = new MobKillListener(this);
        
        // 初始化第三方插件集成
        thirdPartyIntegration = new ThirdPartyIntegration(this);
        Bukkit.getPluginManager().registerEvents(rideMeditationListener, this);
        Bukkit.getPluginManager().registerEvents(mobKillListener, this);

        // 注册命令和TAB补全
        XiuzhenCommand xiuzhenCommand = new XiuzhenCommand(this);
        XiuzhenAdminCommand xiuzhenAdminCommand = new XiuzhenAdminCommand(this);
        
        getCommand("xiuzhen").setExecutor(xiuzhenCommand);
        getCommand("xiuzhen").setTabCompleter(xiuzhenCommand);
        getCommand("xiuzhenadmin").setExecutor(xiuzhenAdminCommand);
        getCommand("xiuzhenadmin").setTabCompleter(xiuzhenAdminCommand);

        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        // 启动修炼定时任务
        startCultivationTask();

        // 启动自动保存任务
        startAutoSaveTask();

        getLogger().info(languageManager.getSystemMessage("plugin_enabled"));
        getLogger().info(languageManager.getSystemMessage("compatible_plugins", "plugins", thirdPartyIntegration.getEnabledPlugins()));
        
        // 注册PlaceholderAPI扩展
        registerPlaceholders();
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleXiuzhen 插件正在禁用...");

        // 保存所有在线玩家的数据
        if (playerDataMap != null) {
            saveAllPlayerData();
        }

        // 关闭数据库连接
        if (databaseManager != null) {
            databaseManager.close();
        }

        // 取消定时任务
        if (cultivationTask != null) {
            cultivationTask.cancel();
        }
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        getLogger().info(languageManager.getSystemMessage("plugin_disabled"));
    }

    /**
     * 启动修炼定时任务（每秒执行）
     */
    private void startCultivationTask() {
        cultivationTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    PlayerData playerData = getPlayerData(playerId);
                    
                    // 检查玩家是否在打坐或坐下
                    boolean isMeditating = playerData.isMeditating();
                    boolean isRiding = rideMeditationListener.isPlayerRiding(playerId);
                    
                    // 修为增长条件：打坐状态或骑乘冥想状态
                    if (isMeditating || isRiding) {
                        // 检查是否移动过多（仅对打坐状态严格检查）
                        boolean canGainExp = true;
                        if (isMeditating && playerData.hasMoved(player.getLocation())) {
                            canGainExp = false;
                            // 通知玩家因为移动而中断打坐
                            player.sendActionBar(ChatColor.RED + "移动中断了打坐修炼！");
                        }
                        
                        if (canGainExp) {
                            // 计算修为增益
                            double baseGain = configManager.getBaseGainPerSecond();
                            double multiplier = 1.0;
                            
                            // 如果是盔甲架冥想状态，应用增益倍数
                            if (isRiding && getConfig().getBoolean("settings.cultivation.armor_stand_meditation.enabled", true)) {
                                multiplier = getConfig().getDouble("settings.cultivation.armor_stand_meditation.multiplier", 2.0);
                            }
                            
                            double gain = baseGain * multiplier;
                            playerData.addExp(gain);
                            
                            // 播放粒子效果
                            playMeditationParticles(player);
                            
                            // 更新位置记录
                            playerData.updateLastLocation(player.getLocation());
                            
                            // 显示增益信息
                            String actionMessage = isRiding ? 
                                ChatColor.GREEN + "骑乘冥想中... (+" + gain + " 修为)" :
                                ChatColor.GREEN + "打坐修炼中... (+" + gain + " 修为)";
                            player.sendActionBar(actionMessage);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L); // 20 ticks = 1秒
    }

    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        int intervalTicks = configManager.getAutoSaveIntervalMinutes() * 1200; // 分钟转ticks (1分钟=1200ticks)
        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveAllPlayerData();
                getLogger().info(languageManager.getSystemMessage("data_saved"));
            }
        }.runTaskTimer(this, intervalTicks, intervalTicks);
    }

    /**
     * 播放打坐粒子效果
     */
    private void playMeditationParticles(Player player) {
        Location location = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location, 10, 0.5, 0.5, 0.5, 0);
    }

    /**
     * 获取玩家数据（如果不存在则创建）
     */
    public PlayerData getPlayerData(UUID playerId) {
        PlayerData data = playerDataMap.get(playerId);
        if (data == null) {
            data = new PlayerData(playerId, this);
            data.loadData();
            playerDataMap.put(playerId, data);
        }
        return data;
    }

    /**
     * 保存所有在线玩家的数据
     */
    private void saveAllPlayerData() {
        for (PlayerData data : playerDataMap.values()) {
            data.saveData();
        }
    }

    /**
     * 命令执行器
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令！");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = getPlayerData(player.getUniqueId());

        if (args.length == 0) {
            // 显示玩家当前状态
            displayPlayerStatus(player, playerData);
            return true;
        }

        if (args.length >= 1) {
            if ("meditate".equalsIgnoreCase(args[0])) {
                // 切换打坐状态
                playerData.toggleMeditation(player);
                
                if (playerData.isMeditating()) {
                    player.sendMessage(ChatColor.GREEN + "开始打坐修炼！");
                    player.sendMessage(ChatColor.YELLOW + "保持静止以获得修为...");
                } else {
                    player.sendMessage(ChatColor.RED + "停止打坐修炼。");
                }
                return true;
            }
        }

        player.sendMessage(ChatColor.RED + "未知的子命令！使用 /xiuzhen 查看状态，/xiuzhen meditate 开始/停止打坐");
        return true;
    }

    /**
     * 显示玩家状态
     */
    private void displayPlayerStatus(Player player, PlayerData playerData) {
        FileConfiguration realmsConfig = configManager.getRealmsConfig();
        
        // 获取境界显示名称
        String realmDisplayName = realmsConfig.getString("realms." + playerData.getCurrentRealmKey() + ".display_name", "未知境界");
        
        // 获取段位名称
        String subLevelPath = "realms." + playerData.getCurrentRealmKey() + ".sub_levels." + playerData.getCurrentSubLevelIndex() + ".name";
        String subLevelName = realmsConfig.getString(subLevelPath, "未知段位");
        
        // 获取当前段位升级所需修为
        String expRequirementPath = "realms." + playerData.getCurrentRealmKey() + ".sub_levels." + playerData.getCurrentSubLevelIndex() + ".required_exp";
        int expRequirement = getConfigInt(realmsConfig, expRequirementPath, 0);
        
        player.sendMessage(ChatColor.GOLD + "=== 修仙状态 ===");
        player.sendMessage(ChatColor.AQUA + "境界: " + ChatColor.WHITE + realmDisplayName + subLevelName);
        player.sendMessage(ChatColor.AQUA + "修为: " + ChatColor.WHITE + playerData.getCurrentExp() + "/" + expRequirement);
        player.sendMessage(ChatColor.AQUA + "状态: " + (playerData.isMeditating() ? 
            ChatColor.GREEN + "打坐中" : ChatColor.RED + "未打坐"));
    }

    /**
     * 监听玩家移动事件
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = getPlayerData(player.getUniqueId());
        
        // 如果玩家在打坐且移动了，则停止打坐
        if (playerData.isMeditating() && playerData.hasMoved(event.getTo())) {
            playerData.setMeditating(false);
            player.sendMessage(ChatColor.RED + "你移动了，打坐被打断！");
        }
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

    // Getter方法
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public PlayerDataDAO getPlayerDataDAO() {
        return playerDataDAO;
    }
    
    public RideMeditationListener getRideMeditationListener() {
        return rideMeditationListener;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    /**
     * 注册PlaceholderAPI扩展
     */
    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new XiuzhenPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI扩展注册成功！");
        } else {
            getLogger().info("未检测到PlaceholderAPI，跳过扩展注册");
        }
    }
    

    
    /**
     * 从内存中移除玩家数据（用于重置功能）
     */
    public void removePlayerData(UUID playerId) {
        playerDataMap.remove(playerId);
    }
}
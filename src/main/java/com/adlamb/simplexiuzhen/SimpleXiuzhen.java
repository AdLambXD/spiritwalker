package com.adlamb.simplexiuzhen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.adlamb.simplexiuzhen.commands.XiuzhenAdminCommand;
import com.adlamb.simplexiuzhen.commands.XiuzhenCommand;
import com.adlamb.simplexiuzhen.database.DatabaseManager;
import com.adlamb.simplexiuzhen.database.PlayerDataDAO;
import com.adlamb.simplexiuzhen.gui.GuiListener;
import com.adlamb.simplexiuzhen.gui.GuiManager;
import com.adlamb.simplexiuzhen.integration.ThirdPartyIntegration;
import com.adlamb.simplexiuzhen.lang.LanguageManager;
import com.adlamb.simplexiuzhen.listeners.MobKillListener;
import com.adlamb.simplexiuzhen.listeners.PlayerQuitListener;
import com.adlamb.simplexiuzhen.listeners.RideMeditationListener;
import com.adlamb.simplexiuzhen.permissions.XiuzhenPermissions;
import com.adlamb.simplexiuzhen.placeholder.XiuzhenPlaceholderExpansion;


/**
 * 主类 - 修仙系统插件
 */
public class SimpleXiuzhen extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PlayerDataDAO playerDataDAO;
    private RideMeditationListener rideMeditationListener;
    private MobKillListener mobKillListener;
    private PlayerQuitListener playerQuitListener;
    private ThirdPartyIntegration thirdPartyIntegration;
    private XiuzhenPermissions permissionsManager;
    private LanguageManager languageManager;
    private EnhancedKungFuManager enhancedKungFuManager;
    private GuiManager guiManager;
    private BreakthroughSystem breakthroughSystem;
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
        
        // 初始化功法管理器
        enhancedKungFuManager = new EnhancedKungFuManager(this);
        
        // 初始化 GUI 管理器
        guiManager = new GuiManager(this);
        
        // 初始化突破系统
        breakthroughSystem = new BreakthroughSystem(this);
        
        // 初始化骑乘冥想监听器
        rideMeditationListener = new RideMeditationListener(this);
        
        // 初始化怪物击杀监听器
        mobKillListener = new MobKillListener(this);
        
        // 初始化玩家退出监听器
        playerQuitListener = new PlayerQuitListener(this);
        
        // 初始化第三方插件集成
        thirdPartyIntegration = new ThirdPartyIntegration(this);
        Bukkit.getPluginManager().registerEvents(rideMeditationListener, this);
        Bukkit.getPluginManager().registerEvents(mobKillListener, this);
        Bukkit.getPluginManager().registerEvents(playerQuitListener, this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);

        // 注册命令和 TAB 补全
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
        
        // 注册 PlaceholderAPI 扩展
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
                    
                    // 检查玩家状态
                    boolean isMeditating = playerData.isMeditating();
                    boolean isRiding = rideMeditationListener.isPlayerRiding(playerId);
                    boolean isInCombatTraining = playerData.isInCombatTraining();
                    boolean isInCombat = playerData.isInCombat();
                    
                    // 修为增长条件：打坐状态、骑乘冥想状态或战斗训练状态
                    boolean canGainXiuzhenExp = isMeditating || isRiding;
                    
                    // 修仙系统修为增长
                    if (canGainXiuzhenExp) {
                        // 检查是否移动过多（仅对打坐状态严格检查）
                        boolean canProceed = true;
                        if (isMeditating && playerData.hasMoved(player.getLocation())) {
                            canProceed = false;
                            // 通知玩家因为移动而中断打坐
                            player.sendActionBar(languageManager.getPlayerMessage("meditate_interrupt"));
                        }
                        
                        if (canProceed) {
                            // 计算修仙修为增益
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
                                languageManager.getPlayerMessage("meditate_riding_actionbar", "gain", gain) :
                                languageManager.getPlayerMessage("meditate_actionbar", "gain", gain);
                            player.sendActionBar(actionMessage);
                        }
                    }
                    
                    // 武者系统修为增长 - 根据用户要求：武者修为不应该自动增长，而应该只在击杀时增长
                    // 因此移除自动增长逻辑，只保留内力消耗和恢复逻辑
                    // 在战斗状态下消耗内力，在非战斗训练状态下恢复内力
                    if (isInCombat) {
                        playerData.consumeNeili(0.5); // 战斗中缓慢消耗内力
                    } else if (isInCombatTraining) {
                        // 非战斗时恢复内力（战斗训练状态也视为非战斗状态）
                        playerData.recoverNeili(0.3);
                    }
                    
                    // 记录战斗时间（仅在真正战斗时）
                    if (isInCombat) {
                        playerData.recordCombatTime();
                    }
                    
                    // 自然资源恢复（非修炼状态时）
                    if (!canGainXiuzhenExp) {
                        // 缓慢恢复灵力和内力
                        playerData.recoverLingli(0.1);
                        playerData.recoverNeili(0.1);
                    }
                }
            }
        }.runTaskTimer(this, 20L, 20L); // 20 ticks = 1 秒
    }

    /**
     * 启动自动保存任务
     */
    private void startAutoSaveTask() {
        int intervalTicks = configManager.getAutoSaveIntervalMinutes() * 1200; // 分钟转 ticks (1 分钟=1200ticks)
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

    // Getter 方法
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
    
    public EnhancedKungFuManager getEnhancedKungFuManager() {
        return enhancedKungFuManager;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }
    
    public BreakthroughSystem getBreakthroughSystem() {
        return breakthroughSystem;
    }
    
    /**
     * 注册 PlaceholderAPI 扩展
     */
    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new XiuzhenPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI 扩展注册成功！");
        } else {
            getLogger().info("未检测到 PlaceholderAPI，跳过扩展注册");
        }
    }
    
    /**
     * 从内存中移除玩家数据（用于重置功能）
     */
    public void removePlayerData(UUID playerId) {
        playerDataMap.remove(playerId);
    }
    
    /**
     * 获取玩家数据映射（用于监听器）
     */
    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }
}

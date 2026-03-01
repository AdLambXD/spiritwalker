package com.adlamb.simplexiuzhen.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import com.adlamb.simplexiuzhen.EnhancedPlayerData;
import com.adlamb.simplexiuzhen.SimpleXiuzhen;
import com.adlamb.simplexiuzhen.lang.LanguageManager;

/**
 * 怪物击杀监听器
 * 处理玩家击杀怪物获得修为的逻辑
 */
public class MobKillListener implements Listener {
    private final SimpleXiuzhen plugin;
    private final LanguageManager languageManager;
    
    // 不同怪物的基础修为奖励
    private static final Map<EntityType, Double> MOB_EXP_REWARDS = new HashMap<>();
    
    static {
        // 被动生物（较低奖励）
        MOB_EXP_REWARDS.put(EntityType.CHICKEN, 0.1);
        MOB_EXP_REWARDS.put(EntityType.COW, 0.1);
        MOB_EXP_REWARDS.put(EntityType.PIG, 0.1);
        MOB_EXP_REWARDS.put(EntityType.SHEEP, 0.1);
        MOB_EXP_REWARDS.put(EntityType.RABBIT, 0.15);
        MOB_EXP_REWARDS.put(EntityType.COD, 0.05);
        MOB_EXP_REWARDS.put(EntityType.SALMON, 0.05);
        MOB_EXP_REWARDS.put(EntityType.TROPICAL_FISH, 0.05);
        MOB_EXP_REWARDS.put(EntityType.PUFFERFISH, 0.05);
        
        // 中立生物（中等奖励）
        MOB_EXP_REWARDS.put(EntityType.WOLF, 0.3);
        MOB_EXP_REWARDS.put(EntityType.IRON_GOLEM, 0.5);
        MOB_EXP_REWARDS.put(EntityType.SNOWMAN, 0.2);
        MOB_EXP_REWARDS.put(EntityType.BEE, 0.2);
        
        // 敌对生物（较高奖励）
        MOB_EXP_REWARDS.put(EntityType.ZOMBIE, 0.4);
        MOB_EXP_REWARDS.put(EntityType.SKELETON, 0.4);
        MOB_EXP_REWARDS.put(EntityType.CREEPER, 0.5);
        MOB_EXP_REWARDS.put(EntityType.SPIDER, 0.35);
        MOB_EXP_REWARDS.put(EntityType.ENDERMAN, 0.6);
        MOB_EXP_REWARDS.put(EntityType.WITCH, 0.7);
        MOB_EXP_REWARDS.put(EntityType.DROWNED, 0.4);
        MOB_EXP_REWARDS.put(EntityType.HUSK, 0.45);
        MOB_EXP_REWARDS.put(EntityType.STRAY, 0.45);
        MOB_EXP_REWARDS.put(EntityType.PHANTOM, 0.55);
        
        // Boss级生物（高奖励）
        MOB_EXP_REWARDS.put(EntityType.ENDER_DRAGON, 50.0);
        MOB_EXP_REWARDS.put(EntityType.WITHER, 30.0);
        MOB_EXP_REWARDS.put(EntityType.ELDER_GUARDIAN, 20.0);
        MOB_EXP_REWARDS.put(EntityType.WARDEN, 25.0);
        
        // 下界生物
        MOB_EXP_REWARDS.put(EntityType.BLAZE, 0.8);
        MOB_EXP_REWARDS.put(EntityType.GHAST, 1.2);
        MOB_EXP_REWARDS.put(EntityType.MAGMA_CUBE, 0.6);
        MOB_EXP_REWARDS.put(EntityType.ZOMBIFIED_PIGLIN, 0.5);
        MOB_EXP_REWARDS.put(EntityType.PIGLIN, 0.3);
        MOB_EXP_REWARDS.put(EntityType.PIGLIN_BRUTE, 0.9);
        
        // 末地生物
        MOB_EXP_REWARDS.put(EntityType.SHULKER, 1.0);
        MOB_EXP_REWARDS.put(EntityType.ENDERMITE, 0.3);
    }
    
    public MobKillListener(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }
    
    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        // 检查是否是玩家击杀的怪物
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            EntityType mobType = event.getEntity().getType();
            
            // 获取修为奖励
            Double expReward = MOB_EXP_REWARDS.get(mobType);
            if (expReward == null) {
                expReward = 0.2; // 默认奖励
            }
            
            // 应用难度倍数（从配置读取）
            double difficultyMultiplier = plugin.getConfig().getDouble("settings.mob_kill.difficulty_multiplier", 1.0);
            expReward *= difficultyMultiplier;
            
            // 应用境界倍数（更强的玩家获得更多修为）
            EnhancedPlayerData enhancedPlayerData = plugin.getEnhancedPlayerData(player.getUniqueId());
            double realmMultiplier = getWushuRealmMultiplier(enhancedPlayerData.getCurrentWushuRealmKey());
            expReward *= realmMultiplier;
            
            // 增加武者修为
            enhancedPlayerData.addWushuExp(expReward);
            
            // 记录战斗状态
            enhancedPlayerData.setInCombat(true);
            
            // 显示获得修为提示
            if (plugin.getConfig().getBoolean("settings.mob_kill.show_exp_message", true)) {
                // 修复浮点数精度问题 - 使用BigDecimal进行精确计算
                java.math.BigDecimal bd = new java.math.BigDecimal(expReward).setScale(2, java.math.RoundingMode.HALF_UP);
                String formattedExp = bd.toString();
                // 显示在动作栏上，与打坐格式一致
                String actionMessage = languageManager.getPlayerMessage("mob_kill_actionbar", "gain", formattedExp, "mob", getMobDisplayName(mobType));
                player.sendActionBar(actionMessage);
            }
        }
    }
    
    /**
     * 根据武者境界获取修为倍数
     */
    private double getWushuRealmMultiplier(String realmKey) {
        switch (realmKey) {
            case "XiuLian": return 1.0;
            case "TongMai": return 1.3;
            case "ZhuJing": return 1.6;
            case "HuaYuan": return 2.2;
            default: return 1.0;
        }
    }
    
    /**
     * 获取怪物的显示名称
     */
    private String getMobDisplayName(EntityType entityType) {
        switch (entityType) {
            case ZOMBIE: return "僵尸";
            case SKELETON: return "骷髅";
            case CREEPER: return "苦力怕";
            case SPIDER: return "蜘蛛";
            case ENDERMAN: return "末影人";
            case WITCH: return "女巫";
            case BLAZE: return "烈焰人";
            case GHAST: return "恶魂";
            case WITHER: return "凋零";
            case ENDER_DRAGON: return "末影龙";
            default: return entityType.name().toLowerCase().replace("_", " ");
        }
    }
}
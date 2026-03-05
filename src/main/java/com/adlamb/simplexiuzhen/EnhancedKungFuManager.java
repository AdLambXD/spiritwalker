package com.adlamb.simplexiuzhen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强版功法管理器 - 支持双系统功法管理
 */
public class EnhancedKungFuManager {
    private final SimpleXiuzhen plugin;
    private final Map<String, EnhancedKungFu> kungFuMap = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> playerKungFus = new ConcurrentHashMap<>();
    private final Map<String, Long> playerCooldowns = new ConcurrentHashMap<>();
    
    public EnhancedKungFuManager(SimpleXiuzhen plugin) {
        this.plugin = plugin;
        loadDefaultKungFus();
    }
    
    /**
     * 加载默认功法
     */
    private void loadDefaultKungFus() {
        // 修仙系统功法
        EnhancedKungFu fireBall = new EnhancedKungFu(
            "fire_ball",
            "火球术",
            "释放一个火球攻击敌人，造成火焰伤害",
            "attack",
            "beginner",
            "xiuzhen",
            "LianQi",
            100.0,
            15.0, // 灵力消耗
            0.0,  // 内力消耗
            60, // 3秒冷却
            Arrays.asList("damage:5", "effect:fire"),
            Map.of("range", 10.0, "power", 1.5)
        );
        
        EnhancedKungFu lightningStrike = new EnhancedKungFu(
            "lightning_strike",
            "雷击术",
            "召唤闪电打击目标，造成电击伤害并有几率麻痹",
            "attack",
            "intermediate",
            "xiuzhen",
            "ZhuJi",
            500.0,
            25.0, // 灵力消耗
            0.0,  // 内力消耗
            120, // 6秒冷却
            Arrays.asList("damage:10", "effect:stun:2"),
            Map.of("range", 15.0, "chance", 0.3)
        );
        
        EnhancedKungFu healingPulse = new EnhancedKungFu(
            "healing_pulse_xiuzhen",
            "仙灵治愈",
            "调动天地灵气治愈伤势",
            "healing",
            "beginner",
            "xiuzhen",
            "LianQi",
            80.0,
            20.0, // 灵力消耗
            0.0,  // 内力消耗
            180, // 9秒冷却
            Arrays.asList("heal:8", "duration:1"),
            Map.of("amount", 8, "radius", 3.0)
        );
        
        // 武者系统功法
        EnhancedKungFu swordArt = new EnhancedKungFu(
            "sword_art",
            "剑心通明",
            "凝聚内力施展精妙剑法",
            "attack",
            "beginner",
            "wushu",
            "XiuLian",
            100.0,
            0.0,  // 灵力消耗
            20.0, // 内力消耗
            50, // 2.5秒冷却
            Arrays.asList("damage:8", "effect:bleed"),
            Map.of("range", 3.0, "combo", 3)
        );
        
        EnhancedKungFu ironBody = new EnhancedKungFu(
            "iron_body",
            "金刚不坏",
            "运功强化肉身防御",
            "defense",
            "intermediate",
            "wushu",
            "TongMai",
            500.0,
            0.0,  // 灵力消耗
            30.0, // 内力消耗
            200, // 10秒冷却
            Arrays.asList("defense:50", "duration:15"),
            Map.of("defense_bonus", 50, "duration", 15)
        );
        
        EnhancedKungFu tigerFist = new EnhancedKungFu(
            "tiger_fist",
            "猛虎下山",
            "爆发全部内力打出致命一击",
            "attack",
            "advanced",
            "wushu",
            "ZhuJing",
            2000.0,
            0.0,  // 灵力消耗
            50.0, // 内力消耗
            300, // 15秒冷却
            Arrays.asList("damage:25", "effect:knockback"),
            Map.of("damage", 25, "knockback", 2.0)
        );
        
        // 辅助型功法
        EnhancedKungFu meditationBoost = new EnhancedKungFu(
            "meditation_boost",
            "入定凝神",
            "深度冥想大幅提升修炼效率",
            "support",
            "beginner",
            "xiuzhen", // 两个系统都可以使用
            "LianQi",
            50.0,
            10.0, // 灵力消耗
            10.0, // 内力消耗
            240, // 12秒冷却
            Arrays.asList("cultivation_bonus:2.0", "duration:30"),
            Map.of("multiplier", 2.0, "duration", 30)
        );
        
        // 添加到功法库
        kungFuMap.put(fireBall.getId(), fireBall);
        kungFuMap.put(lightningStrike.getId(), lightningStrike);
        kungFuMap.put(healingPulse.getId(), healingPulse);
        kungFuMap.put(swordArt.getId(), swordArt);
        kungFuMap.put(ironBody.getId(), ironBody);
        kungFuMap.put(tigerFist.getId(), tigerFist);
        kungFuMap.put(meditationBoost.getId(), meditationBoost);
        
        plugin.getLogger().info("加载了 " + kungFuMap.size() + " 个双系统功法");
    }
    
    /**
     * 获取玩家可以学习的功法
     */
    public List<EnhancedKungFu> getAvailableKungFus(PlayerData playerData) {
        List<EnhancedKungFu> available = new ArrayList<>();
        for (EnhancedKungFu kungFu : kungFuMap.values()) {
            if (kungFu.canLearn(playerData)) {
                available.add(kungFu);
            }
        }
        return available;
    }
    
    /**
     * 获取指定系统的功法
     */
    public List<EnhancedKungFu> getKungFusBySystem(String systemType) {
        List<EnhancedKungFu> result = new ArrayList<>();
        for (EnhancedKungFu kungFu : kungFuMap.values()) {
            if (kungFu.getSystemType().equals(systemType)) {
                result.add(kungFu);
            }
        }
        return result;
    }
    
    /**
     * 获取功法
     */
    public EnhancedKungFu getKungFu(String id) {
        return kungFuMap.get(id);
    }
    
    /**
     * 获取所有功法
     */
    public Collection<EnhancedKungFu> getAllKungFus() {
        return kungFuMap.values();
    }
    
    /**
     * 检查玩家是否可以学习功法
     */
    public boolean canLearnKungFu(UUID playerId, String kungFuId) {
        PlayerData playerData = plugin.getPlayerData(playerId);
        EnhancedKungFu kungFu = getKungFu(kungFuId);
        
        if (kungFu == null) return false;
        
        return kungFu.canLearn(playerData);
    }
    
    /**
     * 学习功法
     */
    public boolean learnKungFu(UUID playerId, String kungFuId) {
        if (!canLearnKungFu(playerId, kungFuId)) {
            return false;
        }
        
        playerKungFus.computeIfAbsent(playerId, k -> new HashSet<>()).add(kungFuId);
        EnhancedKungFu kungFu = getKungFu(kungFuId);
        kungFu.setUnlocked(true);
        
        return true;
    }
    
    /**
     * 使用功法
     */
    public boolean useKungFu(UUID playerId, String kungFuId) {
        PlayerData playerData = plugin.getPlayerData(playerId);
        EnhancedKungFu kungFu = getKungFu(kungFuId);
        
        if (kungFu == null) return false;
        if (!kungFu.isUnlocked()) return false;
        if (isKungFuOnCooldown(playerId, kungFuId)) return false;
        
        // 检查并消耗资源
        if (kungFu.use(playerData)) {
            // 设置冷却时间
            setKungFuCooldown(playerId, kungFuId, kungFu.getCooldownTicks());
            
            // 触发功法效果（简化实现）
            triggerKungFuEffect(playerId, kungFu);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 触发功法效果
     */
    private void triggerKungFuEffect(UUID playerId, EnhancedKungFu kungFu) {
        // 这里应该实现具体的功法效果
        // 简化实现，只是记录日志
        plugin.getLogger().info("玩家 " + playerId + " 使用了功法: " + kungFu.getName());
    }
    
    /**
     * 检查玩家是否拥有功法
     */
    public boolean hasKungFu(UUID playerId, String kungFuId) {
        Set<String> kungFus = playerKungFus.get(playerId);
        return kungFus != null && kungFus.contains(kungFuId);
    }
    
    /**
     * 检查功法冷却时间
     */
    public boolean isKungFuOnCooldown(UUID playerId, String kungFuId) {
        Long cooldownTime = playerCooldowns.get(playerId + ":" + kungFuId);
        if (cooldownTime == null) return false;
        
        return System.currentTimeMillis() < cooldownTime;
    }
    
    /**
     * 设置功法冷却时间
     */
    public void setKungFuCooldown(UUID playerId, String kungFuId, int ticks) {
        long cooldownTime = System.currentTimeMillis() + (ticks * 50L); // 1tick = 50ms
        playerCooldowns.put(playerId.toString() + ":" + kungFuId, cooldownTime);
    }
    
    /**
     * 获取玩家已学习的功法
     */
    public Set<String> getPlayerKungFus(UUID playerId) {
        return playerKungFus.getOrDefault(playerId, Collections.emptySet());
    }
    
    /**
     * 清理过期的冷却时间
     */
    public void cleanupCooldowns() {
        playerCooldowns.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue());
    }
}
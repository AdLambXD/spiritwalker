package com.adlamb.simplexiuzhen;

import java.util.List;
import java.util.Map;

/**
 * 增强版功法类 - 支持内力和灵力消耗
 */
public class EnhancedKungFu {
    private String id;                    // 功法唯一标识
    private String name;                  // 功法名称
    private String description;           // 功法描述
    private String type;                  // 功法类型: attack/defense/support/healing
    private String level;                 // 功法等级: beginner/intermediate/advanced/master
    private String systemType;            // 所属系统: xiuzhen(修仙)/wushu(武者)
    private String realmRequirement;      // 境界要求
    private double expRequirement;        // 修为要求
    private double lingliCost;            // 灵力消耗（修仙功法）
    private double neiliCost;             // 内力消耗（武者功法）
    private int cooldownTicks;            // 冷却时间（ticks）
    private boolean isUnlocked;           // 是否已解锁
    private List<String> effects;         // 效果列表
    private Map<String, Object> config;   // 配置参数
    
    public EnhancedKungFu(String id, String name, String description, String type, String level,
                         String systemType, String realmRequirement, double expRequirement,
                         double lingliCost, double neiliCost, int cooldownTicks,
                         List<String> effects, Map<String, Object> config) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.level = level;
        this.systemType = systemType;
        this.realmRequirement = realmRequirement;
        this.expRequirement = expRequirement;
        this.lingliCost = lingliCost;
        this.neiliCost = neiliCost;
        this.cooldownTicks = cooldownTicks;
        this.effects = effects;
        this.config = config;
        this.isUnlocked = false;
    }
    
    // Getter方法
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getLevel() { return level; }
    public String getSystemType() { return systemType; }
    public String getRealmRequirement() { return realmRequirement; }
    public double getExpRequirement() { return expRequirement; }
    public double getLingliCost() { return lingliCost; }
    public double getNeiliCost() { return neiliCost; }
    public int getCooldownTicks() { return cooldownTicks; }
    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { this.isUnlocked = unlocked; }
    public List<String> getEffects() { return effects; }
    public Map<String, Object> getConfig() { return config; }
    
    // 显示用Getter方法
    public String getTypeDisplay() {
        switch (type.toLowerCase()) {
            case "attack": return "攻击";
            case "defense": return "防御";
            case "support": return "辅助";
            case "healing": return "治疗";
            default: return type;
        }
    }
    
    public String getLevelDisplay() {
        switch (level.toLowerCase()) {
            case "beginner": return "初级";
            case "intermediate": return "中级";
            case "advanced": return "高级";
            case "master": return "大师级";
            default: return level;
        }
    }
    
    public String getSystemTypeDisplay() {
        switch (systemType.toLowerCase()) {
            case "xiuzhen": return "修仙";
            case "wushu": return "武者";
            default: return systemType;
        }
    }
    
    /**
     * 检查是否可以学习此功法
     */
    public boolean canLearn(PlayerData playerData) {
        // 检查系统匹配
        if ("xiuzhen".equals(this.systemType)) {
            if (!playerData.getCurrentRealmKey().equals(this.realmRequirement)) {
                return false;
            }
            return playerData.getCurrentExp() >= this.expRequirement;
        } else if ("wushu".equals(this.systemType)) {
            if (!playerData.getCurrentWushuRealmKey().equals(this.realmRequirement)) {
                return false;
            }
            return playerData.getCurrentWushuExp() >= this.expRequirement;
        }
        return false;
    }
    
    /**
     * 检查是否可以使用此功法
     */
    public boolean canUse(PlayerData playerData) {
        if (!this.isUnlocked) {
            return false;
        }
        
        // 检查资源消耗
        if ("xiuzhen".equals(this.systemType)) {
            return playerData.getCurrentLingli() >= this.lingliCost;
        } else if ("wushu".equals(this.systemType)) {
            return playerData.getCurrentNeili() >= this.neiliCost;
        }
        return false;
    }
    
    /**
     * 使用功法并消耗相应资源
     */
    public boolean use(PlayerData playerData) {
        if (!canUse(playerData)) {
            return false;
        }
        
        // 消耗资源
        if ("xiuzhen".equals(this.systemType)) {
            playerData.consumeLingli(this.lingliCost);
        } else if ("wushu".equals(this.systemType)) {
            playerData.consumeNeili(this.neiliCost);
        }
        
        return true;
    }
}
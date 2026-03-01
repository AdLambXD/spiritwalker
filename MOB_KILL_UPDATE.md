# 怪物击杀机制更新说明

## 🔄 更新概述

本次更新将怪物击杀获得修为的机制从**修仙系统**改为**武者系统**，使武者修炼更加注重实战和战斗。

## 🎯 主要变更

### 1. 修为归属调整
- **原机制**：击杀怪物获得修仙修为（增加 `PlayerData.currentExp`）
- **新机制**：击杀怪物获得武者修为（增加 `EnhancedPlayerData.currentWushuExp`）

### 2. 境界倍数调整
- **原倍数**：基于修仙境界（炼气、筑基、金丹、元婴、化神）
- **新倍数**：基于武者境界（修炼、通脉、筑经、化元）

### 3. 提示消息更新
- **原消息**：绿色提示 "击杀 [怪物] 获得 X 点修为!"
- **新消息**：红色提示 "击杀 [怪物] 获得 X 点武者修为!"

## ⚙️ 技术实现

### 代码变更
```java
// 原代码
PlayerData playerData = plugin.getPlayerData(player.getUniqueId());
double realmMultiplier = getRealmMultiplier(playerData.getCurrentRealmKey());
playerData.addExp(expReward);

// 新代码
EnhancedPlayerData enhancedPlayerData = plugin.getEnhancedPlayerData(player.getUniqueId());
double realmMultiplier = getWushuRealmMultiplier(enhancedPlayerData.getCurrentWushuRealmKey());
enhancedPlayerData.addWushuExp(expReward);
enhancedPlayerData.setInCombat(true);
```

### 境界倍数对照表

**武者境界倍数：**
- 修炼境界 (XiuLian)：1.0倍
- 通脉境界 (TongMai)：1.3倍
- 筑经境界 (ZhuJing)：1.6倍
- 化元境界 (HuaYuan)：2.2倍

## 📋 配置文件更新

### config.yml 新增配置
```yaml
settings:
  mob_kill:
    # 武者境界修为倍数（境界越高，击杀怪物获得越多修为）
    wushu_realm_multipliers:
      XiuLian: 1.0   # 修炼
      TongMai: 1.3   # 通脉
      ZhuJing: 1.6   # 筑经
      HuaYuan: 2.2   # 化元
```

### 语言文件更新
**中文语言文件 (zh_cn.yml)：**
- 添加了武者境界倍数配置
- 优化了相关描述文字

**英文语言文件 (en_us.yml)：**
- Added martial realm multipliers configuration
- Improved related description texts

## 🎮 玩家体验变化

### 修炼重心转移
- **修仙系统**：专注于打坐冥想、盔甲架冥想
- **武者系统**：专注于实战战斗、怪物击杀

### 平衡性调整
- 鼓励玩家通过战斗来提升武者修为
- 修仙修为主要通过静修获得
- 实现真正的双系统差异化发展

### 战斗激励
- 击杀怪物会立即记录战斗状态
- 战斗状态下内力消耗，非战斗状态恢复
- 更强的武者境界获得更高的击杀奖励

## 🔧 服务器管理建议

### 难度调整
```yaml
settings:
  mob_kill:
    difficulty_multiplier: 1.2  # 可根据服务器难度调整
```

### 消息显示控制
```yaml
settings:
  mob_kill:
    show_exp_message: true  # 控制是否显示击杀提示
```

## 📊 预期效果

### 玩家行为引导
- 更多玩家参与PvE战斗
- 武者专精玩家更活跃
- 修仙专精玩家更注重冥想

### 系统平衡
- 避免单一最优发展路径
- 鼓励多元化角色定位
- 增强团队配合需求

## ⚠️ 注意事项

1. **数据兼容性**：此更新不影响已有玩家数据
2. **配置迁移**：建议备份原配置文件后再更新
3. **测试建议**：建议在测试服务器先行验证效果

---

*此次更新进一步完善了双系统的设计理念，让修仙和武者真正走上了不同的发展道路！*
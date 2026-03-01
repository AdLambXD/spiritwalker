# 🧘 SimpleXiuzhen 修仙插件

[![Paper](https://img.shields.io/badge/Paper-1.20.4-blue.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Build Status](https://github.com/yourusername/simplexiuzhen/actions/workflows/build.yml/badge.svg)](https://github.com/yourusername/simplexiuzhen/actions)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Release](https://img.shields.io/github/release/yourusername/simplexiuzhen.svg)](https://github.com/yourusername/simplexiuzhen/releases)

一个功能完整、高度可配置的Minecraft修仙系统插件，为您的服务器带来沉浸式的修仙体验。

## 🌟 核心特色

### 🧘‍♂️ 沉浸式修炼体验
- **多样化修炼方式**：传统打坐 + 盔甲架冥想 + 怪物猎杀
- **智能状态检测**：精准识别玩家修炼状态，防止作弊
- **视觉反馈系统**：粒子效果 + ActionBar提示 + 音效反馈

### 🎮 双系统修炼体验
- **修仙系统**：传统打坐 + 盔甲架冥想 + 怪物猎杀
- **武者系统**：战斗训练 + 内力修炼 + 武技掌握
- **智能状态检测**：精准识别玩家修炼状态，防止作弊
- **视觉反馈系统**：粒子效果 + ActionBar提示 + 音效反馈

### 多语言支持 🌍
- **简体中文** (zh_cn) - 默认语言
- **English** (en_us) - 英文支持
- **自定义翻译** - 用户可修改语言文件
- **参数化消息** - 支持动态内容替换
- **五重境界**：炼气 → 筑基 → 金丹 → 元婴 → 化神
- **细分段位**：每个境界包含多个修炼段位
- **渐进式成长**：平衡的修为需求曲线

### 🔧 强大的扩展支持
- **PlaceholderAPI集成** - 丰富的占位符支持
- **多语言系统** - 简体中文和英文双语支持
- **权限系统** - 完善的权限节点管理
- **配置热重载** - 在线修改配置无需重启

## 🚀 快速开始

### 自动构建 (推荐)
本项目支持GitHub Actions自动构建：
- 每次推送代码自动构建最新版本
- 发布标签时自动创建Release
- 构建产物可在Actions页面下载

### 本地构建
```bash
# Windows
build-local.bat

# Linux/Mac  
./build-local.sh

# 或直接使用Maven
mvn clean package
```

### ProtocolLib集成
由于ProtocolLib不在标准Maven仓库中, 需要手动安装:
1. 从SpigotMC下载 ProtocolLib-5.4.0.jar
2. 执行: `mvn install:install-file -Dfile=ProtocolLib-5.4.0.jar -DgroupId=com.comphenix.protocol -DartifactId=ProtocolLib -Dversion=5.4.0 -Dpackaging=jar`
3. 在pom.xml中添加依赖

### 安装步骤
1. **从Release下载**：访问 [Releases页面](https://github.com/yourusername/simplexiuzhen/releases) 下载最新版本
2. **手动构建**：克隆仓库后运行 `mvn clean package`
3. 将生成的 `target/SimpleXiuzhen-*.jar` 文件放入服务器 `plugins` 目录
4. 重启服务器或使用 `/plugman reload SimpleXiuzhen`
5. 插件将自动生成配置文件

### 基础使用
```bash
# 修仙系统命令
/xiuzhen gui        # 打开图形界面（推荐新手使用）
/xiuzhen            # 查看修仙系统帮助
/xiuzhen meditate   # 开始/停止打坐修炼
/xiuzhen stats      # 查看修仙详细状态
/xiuzhen top        # 查看修仙排行榜
/xiuzhen info       # 查看修仙境界体系

# 武者系统命令
/wushu              # 查看武者系统帮助
/wushu train        # 开始/停止战斗训练
/wushu stats        # 查看武者详细状态
/wushu breakthrough # 尝试武者境界突破
/wushu kungfu list  # 查看可学武者功法
/wushu info         # 查看武者境界体系
```

## 🎮 游戏玩法

### 修炼方式

#### 1. 传统打坐 🧘
```
/xiuzhen meditate  # 开始/停止打坐
```
- 保持静止获得基础修为
- 移动距离过大将中断修炼

#### 2. 盔甲架冥想 ⚔️
```bash
# 生成冥想盔甲架
/summon armor_stand ~ ~ ~ {Invisible:1b,NoBasePlate:1b}
```
- 骑乘盔甲架自动进入冥想状态
- 享受2倍修为增长加成
- 更加舒适自然的修炼体验

#### 3. 战斗修行 ⚔️
- 击杀怪物获得修为奖励
- 不同境界享受不同倍数加成
- 实战中提升修为两不误

### 境界进阶

| 境界 | 段位示例 | 修为要求 | 特殊能力 |
|------|----------|----------|----------|
| 炼气期 | 一层 → 九层 | 0 → 1000 | 基础修炼 |
| 筑基期 | 初期 → 后期 | 1000+ | 属性提升 |
| 金丹期 | 一转 → 九转 | 5000+ | 战斗加成 |
| 元婴期 | 初婴 → 大婴 | 20000+ | 高级技能 |
| 化神期 | 入神 → 大成 | 100000+ | 神通广大 |

## ⚙️ 配置指南

### 主配置文件 (config.yml)
```yaml
settings:
  # 语言设置
  language: "zh_cn"  # 支持: zh_cn (简体中文), en_us (English)
  
  cultivation:
    # 修炼基础设置
    base_gain_per_second: 0.05      # 每秒修为增长
    move_distance_threshold: 0.5    # 打坐中断距离
    
    # 盔甲架冥想配置
    armor_stand_meditation:
      enabled: true
      multiplier: 2.0               # 修为倍数加成
      detection_interval: 10        # 检测频率(ticks)
  
  # 怪物击杀奖励设置
  mob_kill:
    enabled: true
    difficulty_multiplier: 1.0
    show_exp_message: true
    realm_multipliers:
      LianQi: 1.0    # 炼气期倍数
      ZhuJi: 1.2     # 筑基期倍数
      JinDan: 1.5    # 金丹期倍数
      YuanYing: 2.0  # 元婴期倍数
      HuaShen: 2.5   # 化神期倍数
  
  # 数据存储配置
  data:
    storage_type: "YAML"  # YAML 或 MYSQL
    auto_save_interval_minutes: 5
    mysql:
      host: "localhost"
      port: 3306
      database: "simplexiuzhen"
      username: "root"
      password: "password"
```

### 管理员命令
```bash
# 境界管理
/xiuzhenadmin setrealm <玩家> <境界>

# 修为调整
/xiuzhenadmin addexp <玩家> <修为值>

# 系统维护
/xiuzhenadmin reloadconfig    # 重载配置
/xiuzhenadmin backup         # 数据备份
/xiuzhenadmin reset <玩家>   # 重置数据
```

## 🔧 技术规格

### 系统要求
- **Minecraft**: Paper 1.20.4 或更高版本
- **Java**: JDK 17 或更高版本
- **内存**: 建议 2GB+ RAM
- **存储**: 根据玩家数量决定

### 性能特性
- ⚡ **异步数据操作** - 避免主线程阻塞
- 🔄 **智能缓存机制** - 减少数据库查询压力
- 🛡️ **连接池管理** - 高效的数据库连接复用
- 📊 **实时排行榜** - 高性能排序算法

### 兼容性
- ✅ **原生Minecraft** - 完全兼容
- ✅ **主流插件** - 与CMI、EssentialsX等兼容
- ✅ **模组环境** - 支持常见模组服务器

## 📚 文档资源

| 文档 | 用途 | 适用人群 |
|------|------|----------|
| [开发文档](DEVELOPMENT.md) | 技术实现细节 | 开发者 |
| [任务记录](TASK_LOG.md) | 项目发展历程 | 贡献者 |
| 本README | 快速入门指南 | 服务器管理员 |

## 🐛 故障排除

### 常见问题

**Q: 插件无法加载**
A: 检查Java版本(需17+)和Paper版本兼容性

**Q: 数据保存失败**
A: 确认配置文件格式正确，检查存储路径权限

**Q: 命令无响应**
A: 验证玩家权限节点 `simplexiuzhen.use`

**Q: 修炼状态异常**
A: 检查配置中的检测参数是否合理

### 日志分析
```
[SimpleXiuzhen] 插件正在启用...
[SimpleXiuzhen] 数据库连接成功
[SimpleXiuzhen] 配置加载完成
[SimpleXiuzhen] 插件已启用！
```

## 🤝 贡献指南

我们欢迎任何形式的贡献！

### 开发流程
1. Fork 项目仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范
- 遵循Java命名约定
- 使用中文注释
- 保持代码整洁一致
- 编写单元测试

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和测试者！

---

<p align="center">
  <strong>让我们一起在Minecraft世界中体验修仙的乐趣！</strong>
</p>
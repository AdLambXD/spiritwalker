# SimpleXiuzhen 修仙插件开发文档

## 📋 项目概述

SimpleXiuzhen 是一个基于 Java 和 Maven 开发的 Minecraft 修仙系统插件，适用于 Spigot/Paper 1.20.4 服务器。该插件提供了完整的修仙体验，包括境界系统、修为管理、打坐修炼等功能。

## 🎯 核心功能

### 基础修仙系统
- 🧘‍♂️ **打坐修炼** - 玩家可以通过打坐获得修为
- ⚡ **境界系统** - 多层次的修仙境界和段位
- 💾 **数据持久化** - 自动保存玩家修仙进度
- ⚙️ **配置驱动** - 灵活的配置文件管理系统
- ✨ **粒子效果** - 打坐时的视觉反馈

### 增强功能
- 🛡️ **权限系统** - 完整的权限节点管理
- 🏆 **排行榜** - 实时修仙进度排名
- 🔧 **管理员命令** - 完善的后台管理功能
- 🎮 **交互式UI** - 点击式操作界面
- 🪑 **盔甲架冥想** - 骑乘盔甲架自动进入冥想状态

## 🏗️ 技术架构

### 核心组件
```
src/main/java/com/adlamb/simplexiuzhen/
├── SimpleXiuzhen.java           # 主类 - 插件生命周期管理
├── ConfigManager.java           # 配置管理器
├── PlayerData.java              # 玩家数据模型
├── commands/                    # 命令处理器
│   ├── XiuzhenCommand.java      # 玩家命令
│   └── XiuzhenAdminCommand.java # 管理员命令
├── listeners/                   # 事件监听器
│   ├── MobKillListener.java     # 怪物击杀监听
│   └── RideMeditationListener.java # 盔甲架冥想监听
├── database/                    # 数据库相关
│   ├── DatabaseManager.java     # 数据库连接管理
│   └── PlayerDataDAO.java       # 数据访问对象
├── integration/                 # 第三方集成
│   └── ThirdPartyIntegration.java
└── permissions/                 # 权限管理
    └── XiuzhenPermissions.java
```

### 数据存储
- **YAML存储**：适用于小型服务器，简单易用
- **MySQL存储**：适用于大型服务器，支持高并发
- **自动迁移**：支持不同存储类型间的数据迁移

## 🔧 开发环境

### 系统要求
- Java 17 或更高版本
- Maven 3.x
- Paper API 1.20.4

### 依赖库
```xml
<!-- 核心依赖 -->
<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>1.20.4-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>

<!-- 数据库相关 -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

### 编译构建
```bash
# 清理并编译
mvn clean compile

# 打包生成JAR
mvn package

# 运行测试
mvn test
```

## ⚙️ 配置说明

### 主配置文件 (config.yml)
```yaml
settings:
  cultivation:
    # 修为增长设置
    base_gain_per_second: 0.05
    move_distance_threshold: 0.5
    
    # 盔甲架冥想设置
    armor_stand_meditation:
      enabled: true
      multiplier: 2.0
      detection_interval: 10
      dismount_cooldown: 40
  
  # 怪物击杀设置
  mob_kill:
    enabled: true
    difficulty_multiplier: 1.0
    show_exp_message: true
    realm_multipliers:
      LianQi: 1.0
      ZhuJi: 1.2
      JinDan: 1.5
      YuanYing: 2.0
      HuaShen: 2.5
  
  # 数据存储设置
  data:
    auto_save_interval_minutes: 5
    storage_type: "YAML"  # YAML 或 MYSQL
    mysql:
      host: "localhost"
      port: 3306
      database: "simplexiuzhen"
      username: "root"
      password: "password"
```

### 境界配置 (realms.yml)
```yaml
realms:
  LianQi:                          # 炼气境界
    display_name: "炼气"
    level: 1
    sub_levels:
      - name: "一层"
        required_exp: 0
      - name: "二层" 
        required_exp: 50
      # ... 更多段位
```

## 🎮 命令系统

### 玩家命令
```
/xiuzhen                    # 查看基本信息和帮助
/xiuzhen stats             # 查看详细修仙状态
/xiuzhen meditate          # 开始/停止打坐修炼
/xiuzhen top [页码]        # 查看修仙排行榜
/xiuzhen info              # 查看境界体系介绍
```

### 管理员命令
```
/xiuzhenadmin setrealm <玩家> <境界>    # 设置玩家境界
/xiuzhenadmin addexp <玩家> <修为值>    # 添加修为点数
/xiuzhenadmin reloadconfig              # 重载配置文件
/xiuzhenadmin backup                   # 创建数据备份
/xiuzhenadmin reset <玩家>              # 重置玩家数据
```

### 权限节点
- `simplexiuzhen.use` - 使用基础修仙命令（默认开启）
- `simplexiuzhen.admin` - 使用管理员命令（默认OP拥有）

## 🔌 插件集成

### 盔甲架冥想机制
玩家骑乘盔甲架时自动进入冥想状态，获得双倍修为增长：
```bash
# 生成冥想盔甲架
/summon armor_stand ~ ~ ~ {Invisible:1b,NoBasePlate:1b,ShowArms:0b}
```

### 怪物击杀奖励
不同境界的玩家击杀怪物获得不同倍数的修为奖励：
- 炼气期：1.0倍
- 筑基期：1.2倍
- 金丹期：1.5倍
- 元婴期：2.0倍
- 化神期：2.5倍

## 🐛 故障排除

### 常见问题
1. **插件无法加载**：检查Java版本和Paper版本兼容性
2. **数据库连接失败**：确认MySQL服务运行状态和连接参数
3. **命令无响应**：检查玩家权限设置
4. **数据保存失败**：查看日志中的具体错误信息

### 日志分析
```
[SimpleXiuzhen] 插件正在启用...
[SimpleXiuzhen] 数据库连接初始化成功
[SimpleXiuzhen] 配置文件加载完成
[SimpleXiuzhen] 插件已启用！
```

## 📈 性能优化

### 数据库优化
- 使用 HikariCP 连接池
- 异步数据操作
- 智能缓存机制
- 批量保存策略

### 内存管理
- 及时清理离线玩家数据
- 合理设置自动保存间隔
- 优化数据结构减少内存占用

## 🔒 安全考虑

### 数据安全
- 定期备份重要数据
- 管理员命令权限控制
- 敏感配置文件保护

### 代码安全
- 输入参数验证
- 防止SQL注入
- 异常处理机制

## 📚 开发规范

### 代码风格
- 遵循Java命名规范
- 使用中文注释
- 统一代码格式化

### 提交规范
- 功能分支开发
- 详细的提交信息
- 代码审查流程

## 🚀 部署指南

### 生产环境部署
1. 编译生成最终JAR包
2. 配置生产环境参数
3. 部署到服务器plugins目录
4. 启动服务器验证功能

### 版本管理
- 使用语义化版本号
- 维护变更日志
- 提供升级指南

---
*最后更新：2026-02-28*
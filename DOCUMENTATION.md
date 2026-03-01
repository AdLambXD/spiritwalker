# 📚 SimpleXiuzhen 插件完整文档

## 目录
- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [核心功能](#核心功能)
- [功法系统](#功法系统)
- [配置指南](#配置指南)
- [开发指南](#开发指南)
- [部署指南](#部署指南)
- [故障排除](#故障排除)

## 项目概述
SimpleXiuzhen是一个功能完整的Minecraft修仙系统插件，为Paper 1.20.4服务器提供沉浸式的修仙体验。

### 版本信息
- **当前版本**: 1.0-SNAPSHOT
- **支持版本**: Paper 1.20.4+
- **Java要求**: JDK 17+

## 技术架构

### 技术栈
| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 开发语言 |
| Maven | 3.9.5 | 构建工具 |
| Paper API | 1.20.4-R0.1-SNAPSHOT | 核心API |
| HikariCP | 5.0.1 | 数据库连接池 |
| MySQL Connector | 8.0.33 | 数据库驱动 |
| PlaceholderAPI | 2.11.5 | 占位符支持 |

### 项目结构
```
src/
├── main/
│   ├── java/com/adlamb/simplexiuzhen/
│   │   ├── PlayerData.java          # 玩家数据管理
│   │   ├── SimpleXiuzhen.java       # 主插件类
│   │   ├── KungFu.java              # 功法数据模型
│   │   ├── KungFuManager.java       # 功法管理器
│   │   ├── commands/
│   │   │   ├── XiuzhenCommand.java
│   │   │   ├── XiuzhenAdminCommand.java
│   │   │   └── KungFuCommand.java   # 功法命令处理器
│   │   ├── listeners/
│   │   │   ├── RideMeditationListener.java
│   │   │   └── MobKillListener.java
│   │   └── database/
│   │       ├── DatabaseManager.java
│   │       └── PlayerDataDAO.java
│   └── resources/
│       ├── plugin.yml
│       ├── config.yml
│       ├── kungfu.yml             # 功法配置
│       └── lang/
│           ├── zh_cn.yml
│           └── en_us.yml
└── test/                          # 测试代码
```

## 核心功能

### 修炼系统
- **打坐修炼**: 静止获得修为
- **盔甲架冥想**: 骑乘获得2倍修为
- **怪物击杀**: 战斗中获得修为奖励
- **境界体系**: 炼气 → 筑基 → 金丹 → 元婴 → 化神

### 数据管理
- **存储方式**: YAML 或 MySQL
- **自动保存**: 每5分钟自动保存
- **数据迁移**: 支持从YAML到MySQL迁移

### 扩展支持
- **PlaceholderAPI**: 丰富的占位符支持
- **多语言**: 简体中文和英文支持
- **权限系统**: 完善的权限节点管理

## 功法系统

### 功法类型
| 类型 | 示例 | 特点 |
|------|------|------|
| 攻击型 | 火球术、雷击术 | 造成伤害 |
| 防御型 | 能量护盾 | 吸收伤害 |
| 辅助型 | 疾风步 | 提升属性 |
| 治疗型 | 治愈脉冲 | 恢复生命 |

### 功法命令
```bash
/kungfu list        # 查看可用功法
/kungfu learn <id>  # 学习功法
/kungfu use <id>    # 使用功法
/kungfu info <id>   # 查看功法信息
```

### 功法要求
- **境界要求**: 必须达到指定境界
- **修为要求**: 需要足够的修为值
- **冷却时间**: 使用后需要等待冷却

## 配置指南

### 主配置 (config.yml)
```yaml
settings:
  cultivation:
    base_gain_per_second: 0.05      # 基础修为增长速度
    move_distance_threshold: 0.5    # 打坐移动阈值
    armor_stand_meditation:
      enabled: true
      multiplier: 2.0               # 盔甲架倍数
  mob_kill:
    enabled: true
    difficulty_multiplier: 1.0
  data:
    storage_type: "YAML"            # 存储类型
```

### 功法配置 (kungfu.yml)
```yaml
kungfus:
  fire_ball:
    name: "火球术"
    description: "释放一个火球攻击敌人"
    type: "attack"
    level: "beginner"
    realm_requirement: "LianQi"
    exp_requirement: 100.0
    cooldown_ticks: 60
    effects:
      - "damage:5"
      - "effect:fire"
```

## 开发指南

### 添加新功法
1. 在 `kungfu.yml` 中添加新的功法配置
2. 在 `KungFuManager.java` 中添加对应的功法逻辑
3. 更新命令处理器处理新功法效果

### 扩展功能
- **自定义效果**: 修改 `KungFu` 类的 effects 字段
- **新增类型**: 在 `KungFuCommand.java` 中添加新的处理逻辑
- **权限控制**: 在 `XiuzhenPermissions.java` 中添加新的权限节点

## 部署指南

### 本地构建
```bash
# Windows
build-local.bat

# Linux/Mac
./build-local.sh
```

### 服务器部署
1. 将 `target/SimpleXiuzhen-*.jar` 复制到服务器 plugins 目录
2. 启动服务器
3. 插件会自动生成配置文件

### ProtocolLib集成
1. 下载 ProtocolLib-5.4.0.jar
2. 安装到本地Maven仓库
3. 添加依赖到 pom.xml

## 故障排除

### 常见问题
| 问题 | 解决方案 |
|------|----------|
| 插件无法加载 | 检查Java版本(17+)和Paper版本兼容性 |
| 数据保存失败 | 确认配置文件格式正确，检查存储路径权限 |
| 命令无响应 | 验证玩家权限节点 `simplexiuzhen.use` |
| 修炼状态异常 | 检查配置中的检测参数是否合理 |

### 日志分析
```
[SimpleXiuzhen] 插件正在启用...
[SimpleXiuzhen] 数据库连接成功
[SimpleXiuzhen] 配置加载完成
[SimpleXiuzhen] 插件已启用！
```

## 贡献指南
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
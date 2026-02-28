# SimpleXiuzhen 修仙插件

一个基于Java和Maven开发的Minecraft修仙系统插件，适用于Spigot/Paper 1.20.4服务器。

## 功能特性

- 🧘‍♂️ **打坐修炼** - 玩家可以通过打坐获得修为
- ⚡ **境界系统** - 多层次的修仙境界和段位
- 💾 **数据持久化** - 自动保存玩家修仙进度
- ⚙️ **配置驱动** - 灵活的配置文件管理系统
- ✨ **粒子效果** - 打坐时的视觉反馈

## 安装说明

1. 将 `SimpleXiuzhen-1.0-SNAPSHOT.jar` 文件放入服务器的 `plugins` 目录
2. 重启服务器或使用 `/reload` 命令
3. 插件会自动生成配置文件

## 使用方法

### 基本命令

- `/xiuzhen` - 查看当前修仙状态
- `/xiuzhen meditate` - 开始/停止打坐修炼

### 游戏机制

1. **开始修炼**：使用 `/xiuzhen meditate` 命令开始打坐
2. **保持静止**：打坐期间需要保持不动才能获得修为
3. **修为增长**：每秒获得配置设定的修为值
4. **境界提升**：修为达到要求后自动晋升到更高段位或境界
5. **中断机制**：移动超过设定距离会中断打坐状态

## 配置文件

### config.yml
```yaml
settings:
  cultivation:
    base_gain_per_second: 5          # 每秒获得的修为值
    move_distance_threshold: 0.5     # 打坐中断的移动阈值
    
  data:
    auto_save_interval_minutes: 5    # 自动保存间隔（分钟）
```

### realms.yml
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

## 技术架构

### 核心类说明

- **SimpleXiuzhen.java** - 主类，处理插件生命周期和命令
- **ConfigManager.java** - 配置文件管理器
- **PlayerData.java** - 玩家数据管理类
- **plugin.yml** - 插件基本信息配置

### 数据存储

玩家数据保存在 `plugins/SimpleXiuzhen/player_data/` 目录下，每个玩家一个YAML文件。

## 开发环境

- Java 21
- Maven 3.x
- Paper API 1.20.4

### 编译项目
```bash
mvn clean package
```

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request来改进这个插件！
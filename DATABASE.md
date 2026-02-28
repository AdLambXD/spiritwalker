# SimpleXiuzhen 修仙插件 - 数据库增强版

## 新增功能 🚀

### 🗄️ 数据库存储支持
- **SQLite**：适合小型服务器，无需额外配置
- **MySQL**：适合大型服务器，支持高并发

### 📊 数据结构优化
- 玩家基础数据表 (`player_data`)
- 玩家属性表 (`player_attributes`) 
- 玩家功法表 (`player_skills`)
- 玩家物品表 (`player_items`)

## 配置说明 ⚙️

### config.yml 数据库配置
```yaml
settings:
  data:
    # 存储类型: YAML, SQLITE, MYSQL
    storage_type: "SQLITE"
    
    # SQLite 配置
    sqlite:
      database_path: "db/simplexiuzhen.db"
      
    # MySQL 配置
    mysql:
      host: "localhost"
      port: 3306
      database: "simplexiuzhen"
      username: "root"
      password: "password"
      # 连接池配置
      maximum_pool_size: 10
      minimum_idle: 2
      connection_timeout: 30000
      idle_timeout: 600000
      max_lifetime: 1800000
```

## 数据库表结构 📋

### player_data 表
| 字段名 | 类型 | 说明 |
|--------|------|------|
| uuid | VARCHAR(36) | 玩家UUID（主键） |
| current_realm | VARCHAR(50) | 当前境界 |
| current_sub_level | INTEGER | 当前段位索引 |
| current_exp | INTEGER | 当前修为值 |
| is_meditating | BOOLEAN | 是否在打坐 |
| last_location_x/y/z | DOUBLE | 上次位置坐标 |
| last_location_world | VARCHAR(100) | 上次所在世界 |

### player_attributes 表
| 字段名 | 类型 | 说明 |
|--------|------|------|
| uuid | VARCHAR(36) | 玩家UUID（外键） |
| max_health | DOUBLE | 最大生命值 |
| max_mana | INTEGER | 最大灵力值 |
| move_speed | DOUBLE | 移动速度 |
| damage_reduction | DOUBLE | 伤害减免 |

## 性能优化 💪

- 使用 HikariCP 连接池提高数据库性能
- 异步数据操作防止主线程阻塞
- 智能缓存减少数据库查询次数
- 自动连接重试机制

## 迁移指南 🔄

### 从YAML迁移到数据库
1. 设置 `storage_type` 为 `SQLITE` 或 `MYSQL`
2. 插件会自动创建表结构
3. 首次启动时会迁移现有YAML数据
4. 建议备份原数据文件

### 数据库切换
- 可在运行时通过命令切换存储类型
- 支持数据导出/导入功能
- 提供迁移脚本工具

## 故障排除 ❓

### 常见问题
1. **连接失败**：检查数据库服务是否运行
2. **权限不足**：确认MySQL用户有相应权限
3. **表不存在**：插件会自动创建，如失败请手动执行SQL

### 日志查看
```
[SimpleXiuzhen] 数据库连接初始化成功: SQLITE
[SimpleXiuzhen] 数据表创建完成
[SimpleXiuzhen] 批量保存玩家数据到数据库
```

## 开发者信息 👨‍💻

- **数据库管理器**：`DatabaseManager.java`
- **数据访问对象**：`PlayerDataDAO.java`
- **连接池**：HikariCP 5.0.1
- **SQLite驱动**：3.42.0.0
- **MySQL驱动**：8.0.33
# PlaceholderAPI 支持文档

SimpleXiuzhen 插件完全支持 PlaceholderAPI，提供丰富的修仙相关信息占位符。

## 📋 可用占位符

### 基础信息
| 占位符 | 描述 | 示例输出 |
|--------|------|----------|
| `%simplexiuzhen_realm%` | 当前境界Key | `LianQi` |
| `%simplexiuzhen_realm_display%` | 境界显示名称 | `炼气` |
| `%simplexiuzhen_sublevel_index%` | 当前段位索引 | `2` |
| `%simplexiuzhen_sublevel_name%` | 当前段位名称 | `三层` |

### 修为相关
| 占位符 | 描述 | 示例输出 |
|--------|------|----------|
| `%simplexiuzhen_exp%` | 当前修为值(小数) | `1234.56` |
| `%simplexiuzhen_exp_int%` | 当前修为值(整数) | `1234` |
| `%simplexiuzhen_formatted_exp%` | 格式化修为值 | `1.23K` |
| `%simplexiuzhen_next_level_exp%` | 下级所需修为 | `2000` 或 `已满级` |
| `%simplexiuzhen_progress_percentage%` | 当前进度百分比 | `61.73` |

### 状态信息
| 占位符 | 描述 | 示例输出 |
|--------|------|----------|
| `%simplexiuzhen_is_meditating%` | 是否正在打坐 | `true` 或 `false` |
| `%simplexiuzhen_meditation_status%` | 打坐状态文字 | `打坐中` 或 `未打坐` |
| `%simplexiuzhen_status_icon%` | 状态图标 | `🧘` 或 `❌` |

### 属性加成
| 占位符 | 描述 | 示例输出 |
|--------|------|----------|
| `%simplexiuzhen_health_bonus%` | 生命值加成 | `6.0` |
| `%simplexiuzhen_mana_bonus%` | 灵力加成 | `15` |
| `%simplexiuzhen_total_attributes%` | 总属性加成 | `生命+6.0, 灵力+15` |

### 综合信息
| 占位符 | 描述 | 示例输出 |
|--------|------|----------|
| `%simplexiuzhen_formatted_realm%` | 完整境界显示 | `炼气三层` |
| `%simplexiuzhen_cultivation_speed%` | 修炼速度 | `0.10` |
| `%simplexiuzhen_rank_position%` | 排行榜位置 | `#999` |

## 🎮 使用示例

### 在聊天中显示
```
我现在的境界是 %simplexiuzhen_formatted_realm%，修为 %simplexiuzhen_formatted_exp%
```
输出：`我现在的境界是 炼气三层，修为 1.23K`

### 在记分板中使用
```yaml
scoreboard:
  title: "&6修仙状态"
  lines:
    - "&a境界: %simplexiuzhen_formatted_realm%"
    - "&b修为: %simplexiuzhen_formatted_exp%"
    - "&c进度: %simplexiuzhen_progress_percentage%%"
```

### 在全息投影中显示
```
[player]'s Cultivation Status
Realm: %simplexiuzhen_realm_display%
Level: %simplexiuzhen_sublevel_name%
Experience: %simplexiuzhen_exp%
Status: %simplexiuzhen_meditation_status%
```

### 在NPC对话中使用
```
欢迎来到修仙世界！
你的当前境界是 %simplexiuzhen_formatted_realm%
修为进度: %simplexiuzhen_progress_percentage%%
%simplexiuzhen_is_meditating% == "true" ? "正在修炼中..." : "还未开始修炼"
```

## ⚙️ 配置说明

### 修为格式化
- 自动将大数值转换为 K(千) 或 M(百万) 单位
- 保留两位小数精度
- 例如：`1500` → `1.5K`, `2500000` → `2.5M`

### 进度计算
- 基于当前修为与下一级所需修为的比例
- 最大值为 100%
- 满级时显示 100%

## 🔧 开发者信息

### 扩展类
- **类名**: `XiuzhenPlaceholderExpansion`
- **标识符**: `simplexiuzhen`
- **版本**: 与插件版本同步

### 自定义占位符
如果您需要添加自定义占位符，可以继承 `XiuzhenPlaceholderExpansion` 类并重写 `onRequest` 方法。

### 故障排除
1. 确保 PlaceholderAPI 插件已正确安装并启用
2. 检查插件加载顺序，SimpleXiuzhen 应在 PlaceholderAPI 之后加载
3. 使用 `/papi parse <player> %simplexiuzhen_realm%` 命令测试占位符

## 📚 相关链接
- [PlaceholderAPI 官方文档](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki)
- [SimpleXiuzhen GitHub](https://github.com/your-repo/simplexiuzhen)
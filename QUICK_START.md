# 🚀 SimpleXiuzhen 快速入门指南

## 1. 安装插件
1. 下载最新版本的 `SimpleXiuzhen-*.jar`
2. 将文件放入服务器 `plugins` 目录
3. 重启服务器

## 2. 基础命令
```bash
/xiuzhen          # 查看个人信息
/xiuzhen meditate # 开始/停止打坐
/xiuzhen top      # 查看排行榜
/xiuzhen info     # 了解境界体系

/kungfu list      # 查看可用功法
/kungfu use fire_ball  # 使用火球术
```

## 3. 修炼方式
| 方式 | 操作 | 效果 |
|------|------|------|
| 打坐 | `/xiuzhen meditate` | 基础修为增长 |
| 盔甲架冥想 | 骑乘盔甲架 | 2倍修为增长 |
| 怪物击杀 | 击杀怪物 | 获得额外修为 |

## 4. 功法系统
- **火球术**: 攻击型，初级，需要100修为
- **能量护盾**: 防御型，初级
- **疾风步**: 辅助型，提升移动速度
- **治愈脉冲**: 治疗型

## 5. 配置修改
编辑 `config.yml` 文件：
- 修改修为增长速度: `base_gain_per_second`
- 调整打坐检测距离: `move_distance_threshold`
- 切换存储方式: `storage_type`

## 6. 故障排查
- 插件无法加载 → 检查Java版本(17+)和Paper版本
- 数据保存失败 → 检查配置文件格式
- 命令无响应 → 检查权限设置

> 💡 提示: 使用 `/xiuzhen admin reloadconfig` 可以热重载配置
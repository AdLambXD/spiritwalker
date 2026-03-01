# 🚀 部署指南

## GitHub Actions 自动构建

### 设置步骤

1. **创建GitHub仓库**
   ```bash
   # 如果还没有远程仓库，创建一个新的
   git remote add origin https://github.com/yourusername/your-repo-name.git
   git branch -M main
   git push -u origin main
   ```

2. **配置仓库设置**
   - 访问仓库的Settings → Webhooks & Services
   - 确保GitHub Actions已启用

3. **触发构建**
   - 推送代码到main分支
   - 创建新的tag版本
   - 手动触发workflow

### 构建产物

每次成功构建都会生成：
- `SimpleXiuzhen-1.0-SNAPSHOT.jar` - 原始插件文件
- `SimpleXiuzhen-1.0-SNAPSHOT-shaded.jar` - 包含所有依赖的完整插件

### 发布版本

创建tag来发布正式版本：
```bash
git tag -a v1.0.0 -m "第一个正式版本"
git push origin v1.0.0
```

这将自动创建GitHub Release并上传构建好的插件文件。

## 本地构建

### Windows系统
```cmd
build-local.bat
```

### Linux/Mac系统
```bash
chmod +x build-local.sh
./build-local.sh
```

### 手动构建命令
```bash
mvn clean package
```

## 服务器部署

### 1. 准备工作
- 确保服务器运行Paper 1.20.4或更高版本
- Java版本17或更高
- 至少2GB可用内存

### 2. 安装插件
```bash
# 复制插件文件到服务器
cp target/SimpleXiuzhen-1.0-SNAPSHOT-shaded.jar /path/to/server/plugins/

# 重启服务器
./start.sh
```

### 3. 配置数据库（可选）
如果使用MySQL存储，在`config.yml`中配置：
```yaml
settings:
  data:
    storage_type: "MYSQL"
    mysql:
      host: "localhost"
      port: 3306
      database: "simplexiuzhen"
      username: "your_username"
      password: "your_password"
```

### 4. 权限配置
在权限插件中给玩家分配权限：
```
simplexiuzhen.use          # 使用基本命令
simplexiuzhen.admin        # 管理员命令
simplexiuzhen.bypass.*     # 绕过限制
```

## 监控和维护

### 日志监控
```bash
# 查看插件日志
tail -f logs/latest.log | grep SimpleXiuzhen
```

### 性能监控
- 监控内存使用情况
- 检查数据库连接状态
- 观察玩家在线时的CPU使用率

### 常见问题排查

**插件无法加载**
- 检查Java版本兼容性
- 确认Paper版本支持
- 查看完整错误日志

**数据保存问题**
- 验证数据库连接配置
- 检查文件权限
- 确认磁盘空间充足

**性能问题**
- 调整配置中的检测间隔
- 优化数据库索引
- 考虑使用缓存机制

## 更新策略

### 平滑升级
1. 备份现有数据
2. 下载新版本插件
3. 替换旧插件文件
4. 重启服务器
5. 验证功能正常

### 回滚方案
- 保留旧版本插件文件
- 备份配置文件和数据
- 准备回滚脚本

## 最佳实践

### 开发环境
- 使用本地构建脚本快速测试
- 在测试服务器验证新功能
- 保持版本控制的良好习惯

### 生产环境
- 定期备份重要数据
- 监控插件性能指标
- 及时应用安全更新
- 维护详细的变更日志

### 社区支持
- 建立用户反馈渠道
- 提供详细的使用文档
- 定期收集改进建议
- 维护活跃的社区交流
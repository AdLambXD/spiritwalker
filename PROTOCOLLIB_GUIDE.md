# ProtocolLib 使用指南

## 当前状态

由于ProtocolLib在标准Maven仓库中难以获取，项目目前没有包含ProtocolLib依赖。但您可以选择以下几种方式来使用ProtocolLib功能。

## 方案一：手动安装ProtocolLib插件（推荐）

1. **下载ProtocolLib插件**
   - 访问 [ProtocolLib Spigot页面](https://www.spigotmc.org/resources/protocollib.1997/)
   - 下载适合您服务器版本的ProtocolLib插件

2. **安装到服务器**
   - 将下载的ProtocolLib.jar放入服务器的plugins文件夹
   - 重启服务器

3. **在代码中使用**
   ```java
   // 检查ProtocolLib是否可用
   if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
       ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
       // 使用ProtocolLib功能
   }
   ```

## 方案二：本地Maven安装

如果您需要在开发环境中使用ProtocolLib：

1. **下载ProtocolLib JAR文件**
   - 从官方网站或其他可信源获取ProtocolLib.jar

2. **安装到本地Maven仓库**
   ```bash
   mvn install:install-file -Dfile=ProtocolLib.jar -DgroupId=com.comphenix.protocol -DartifactId=ProtocolLib -Dversion=5.4.0 -Dpackaging=jar
   ```

3. **在pom.xml中添加依赖**
   ```xml
   <dependency>
       <groupId>com.comphenix.protocol</groupId>
       <artifactId>ProtocolLib</artifactId>
       <version>5.4.0</version>
       <scope>provided</scope>
   </dependency>
   ```

## 注意事项

- ProtocolLib需要作为独立插件安装在服务器上
- 在代码中使用前请检查插件是否存在
- 不同版本的ProtocolLib可能有API差异
- 建议在生产环境中使用稳定版本

## 替代方案

如果不使用ProtocolLib，也可以考虑：
- 使用原生Bukkit API实现类似功能
- 寻找其他轻量级的数据包处理库
- 直接操作NMS（不推荐，版本兼容性差）
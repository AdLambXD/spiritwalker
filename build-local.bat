@echo off
echo ========================================
echo SimpleXiuzhen 插件本地构建脚本
echo ========================================

echo 正在清理旧构建...
call mvn clean

echo 正在编译项目...
call mvn compile

echo 正在打包插件...
call mvn package

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 构建成功！
    echo 插件文件位置: target\SimpleXiuzhen-*.jar
    echo ========================================
    echo.
    echo 构建产物说明:
    echo - SimpleXiuzhen-1.0-SNAPSHOT.jar: 原始jar文件
    echo - SimpleXiuzhen-1.0-SNAPSHOT-shaded.jar: 包含所有依赖的完整jar
    echo.
    pause
) else (
    echo.
    echo ========================================
    echo 构建失败！请检查错误信息
    echo ========================================
    echo.
    pause
)
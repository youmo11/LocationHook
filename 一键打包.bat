@echo off
chcp 65001
title LocationHook 一键打包工具

echo ==========================================
echo    LocationHook 自动打包工具
echo    目标: Android 16 (API 36)
echo ==========================================
echo.

:: 检查Java环境
echo [1/5] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Java环境!
    echo 请先安装JDK 17并配置环境变量
    pause
    exit /b 1
)
echo ✅ Java环境正常

:: 检查Gradle
echo.
echo [2/5] 检查Gradle...
if not exist "gradlew.bat" (
    echo ❌ 错误: 未找到gradlew.bat
    echo 请确保在项目根目录运行此脚本
    pause
    exit /b 1
)
echo ✅ Gradle配置正常

:: 清理旧构建
echo.
echo [3/5] 清理旧构建文件...
if exist "app\build" (
    rmdir /s /q "app\build"
)
echo ✅ 清理完成

:: 开始编译
echo.
echo [4/5] 开始编译APK (Android 16, API 36)...
echo 这可能需要3-5分钟，请耐心等待...
echo.

gradlew.bat assembleDebug --no-daemon

if errorlevel 1 (
    echo.
    echo ❌ 编译失败!
    echo 请查看上方错误信息
    pause
    exit /b 1
)

:: 检查APK生成
echo.
echo [5/5] 检查APK文件...
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if not exist "%APK_PATH%" (
    echo ❌ 未找到生成的APK文件
    pause
    exit /b 1
)

echo.
echo ==========================================
echo    ✅ 打包成功!
echo ==========================================
echo.
echo APK文件位置:
echo %CD%\%APK_PATH%
echo.
echo 文件大小:
for %%I in ("%APK_PATH%") do echo %%~zI 字节
echo.
echo 安装命令:
echo adb install "%APK_PATH%"
echo.
echo 下一步:
echo 1. 安装APK到设备
echo 2. 在LSPosed中启用模块
echo 3. 重启设备
echo.

:: 打开APK所在文件夹
echo 正在打开文件夹...
start explorer /select,"%CD%\%APK_PATH%"

pause

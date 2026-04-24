@echo off
chcp 65001
echo ==========================================
echo    LocationHook APK 构建脚本
echo ==========================================
echo.

:: 检查Gradle
call gradlew.bat --version >nul 2>&1
if errorlevel 1 (
    echo [错误] Gradle未安装，请先运行 init-gradle.bat
    pause
    exit /b 1
)

echo [1/4] 清理旧构建...
call gradlew.bat clean --quiet
if errorlevel 1 (
    echo [警告] 清理失败，继续尝试构建...
)

echo.
echo [2/4] 构建Debug APK...
call gradlew.bat assembleDebug --no-daemon --parallel --build-cache --configure-on-demand
if errorlevel 1 (
    echo.
    echo [错误] 构建失败！
    echo 常见原因：
    echo 1. 缺少Android SDK - 请安装Android Studio
    echo 2. 网络问题 - 尝试切换国内镜像
    echo 3. 代码错误 - 检查控制台输出的错误信息
    pause
    exit /b 1
)

echo.
echo [3/4] 验证构建结果...
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo [错误] APK文件未生成！
    pause
    exit /b 1
)

echo.
echo [4/4] 构建成功！
echo ==========================================
echo    ✅ APK构建完成！
echo ==========================================
echo.
echo 文件位置：
echo   app\build\outputs\apk\debug\app-debug.apk
echo.
for %%I in ("app\build\outputs\apk\debug\app-debug.apk") do (
    echo 文件大小：%%~zI 字节 (%%~zI / 1024 / 1024 MB)
)
echo.
echo 安装方法：
echo   1. 将APK传输到手机
echo   2. 在手机上安装APK
echo   3. 在LSPosed中启用模块
echo   4. 重启手机
echo.
pause

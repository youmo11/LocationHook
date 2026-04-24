@echo off
chcp 65001
cd /d "%~dp0"

echo =========================================
echo   LocationHook APK 自动构建脚本
echo =========================================
echo.

echo [1/5] 配置Gradle国内镜像...
if not exist "gradle\wrapper" mkdir "gradle\wrapper"
echo distributionBase=GRADLE_USER_HOME> "gradle\wrapper\gradle-wrapper.properties"
echo distributionPath=wrapper/dists>> "gradle\wrapper\gradle-wrapper.properties"
echo distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip>> "gradle\wrapper\gradle-wrapper.properties"
echo zipStoreBase=GRADLE_USER_HOME>> "gradle\wrapper\gradle-wrapper.properties"
echo zipStorePath=wrapper/dists>> "gradle\wrapper\gradle-wrapper.properties"
echo ✅ 已配置腾讯云镜像
echo.

echo [2/5] 下载Gradle并同步项目...
call gradlew.bat --version --quiet 2>nul
if errorlevel 1 (
    echo 首次运行，正在下载Gradle（使用腾讯云镜像）...
    call gradlew.bat --version
)
if errorlevel 1 (
    echo ❌ Gradle下载失败，请检查网络连接
    pause
    exit /b 1
)
echo ✅ Gradle准备就绪
echo.

echo [3/5] 清理旧构建...
call gradlew.bat clean --quiet 2>nul
echo ✅ 清理完成
echo.

echo [4/5] 构建Debug APK（使用腾讯云镜像加速）...
echo 这可能需要3-5分钟，请耐心等待...
call gradlew.bat assembleDebug --parallel --build-cache --configure-on-demand --no-daemon --quiet
if errorlevel 1 (
    echo.
    echo ❌ 构建失败！正在重试（不使用缓存）...
    call gradlew.bat clean assembleDebug --parallel --no-daemon
    if errorlevel 1 (
        echo.
        echo ❌ 构建失败！请查看错误信息。
        pause
        exit /b 1
    )
)
echo.

echo [5/5] 验证构建结果...
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ❌ 未找到生成的APK文件！
    pause
    exit /b 1
)
echo ✅ APK构建成功！
echo.

echo =========================================
echo    ✅ 构建完成！
echo =========================================
echo.
echo APK文件位置：
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
echo 按任意键退出...
pause > nul

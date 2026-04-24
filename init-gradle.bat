@echo off
chcp 65001
echo [1/4] 配置Gradle国内镜像...
mkdir .gradle 2>nul
echo org.gradle.daemon=true> .gradle\gradle.properties
echo org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8>> .gradle\gradle.properties
echo systemProp.http.proxyHost=>> .gradle\gradle.properties
echo systemProp.https.proxyHost=>> .gradle\gradle.properties
echo.
echo [2/4] 下载Gradle Wrapper...
call gradlew.bat --version 2>nul
if errorlevel 1 (
    echo 正在下载Gradle...
    powershell -Command "& {$url='https://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip';$output='gradle.zip';Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing;Expand-Archive -Path $output -DestinationPath '.gradle' -Force;Remove-Item $output}"
)
echo.
echo [3/4] 验证安装...
call gradlew.bat --version
if errorlevel 1 (
    echo [错误] Gradle安装失败
    exit /b 1
)
echo.
echo [4/4] 完成！
echo Gradle环境已配置完成。
pause

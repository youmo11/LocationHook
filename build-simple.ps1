# 简化的构建脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LocationHook 快速构建脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Java
Write-Host "[1/4] 检查Java环境..." -ForegroundColor Yellow
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Host "  ❌ 未找到Java，尝试安装..." -ForegroundColor Red
    # 这里可以添加自动安装Java的逻辑
    Write-Host "  请手动安装JDK 17后再试" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ Java已安装" -ForegroundColor Green

# 检查Gradle Wrapper
Write-Host "[2/4] 检查Gradle Wrapper..." -ForegroundColor Yellow
$wrapperJar = "gradle\wrapper\gradle-wrapper.jar"
if (-not (Test-Path $wrapperJar)) {
    Write-Host "  正在下载Gradle Wrapper..." -ForegroundColor Yellow
    # 使用国内镜像下载
    $wrapperUrl = "https://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip"
    try {
        Invoke-WebRequest -Uri $wrapperUrl -OutFile "$env:TEMP\gradle.zip" -UseBasicParsing
        Expand-Archive -Path "$env:TEMP\gradle.zip" -DestinationPath "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.2-bin" -Force
        Remove-Item "$env:TEMP\gradle.zip"
        Write-Host "  ✅ Gradle下载完成" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ 下载失败，请检查网络" -ForegroundColor Red
        exit 1
    }
}
Write-Host "  ✅ Gradle Wrapper已就绪" -ForegroundColor Green

# 开始构建
Write-Host "[3/4] 开始构建APK..." -ForegroundColor Yellow
Write-Host "  这可能需要5-10分钟，请耐心等待..." -ForegroundColor Gray

# 执行构建
& .\gradlew.bat clean assembleDebug --parallel --build-cache --configure-on-demand --no-daemon 2>&1 | ForEach-Object {
    if ($_ -match "BUILD|FAILED|SUCCESS|ERROR|task|Download") {
        Write-Host "    $_" -ForegroundColor Gray
    }
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "  ❌ 构建失败！" -ForegroundColor Red
    Write-Host "  正在尝试备用方案..." -ForegroundColor Yellow
    
    # 尝试不使用缓存重新构建
    & .\gradlew.bat clean assembleDebug --no-daemon 2>&1 | ForEach-Object {
        if ($_ -match "BUILD|FAILED|SUCCESS|ERROR") {
            Write-Host "    $_" -ForegroundColor Gray
        }
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "  ❌ 构建失败！请检查错误信息。" -ForegroundColor Red
        exit 1
    }
}

# 验证结果
Write-Host "[4/4] 验证构建结果..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "  ❌ 未找到生成的APK文件！" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length
$apkSizeMB = [math]::Round($apkSize / 1MB, 2)

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ 构建成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📦 APK文件信息：" -ForegroundColor Yellow
Write-Host "  位置: $apkPath" -ForegroundColor White
Write-Host "  大小: $apkSize 字节 ($apkSizeMB MB)" -ForegroundColor White
Write-Host ""
Write-Host "📱 安装方法：" -ForegroundColor Yellow
Write-Host "  1. 将APK文件传输到手机" -ForegroundColor Gray
Write-Host "  2. 在手机上安装APK" -ForegroundColor Gray
Write-Host "  3. 在LSPosed中启用LocationHook模块" -ForegroundColor Gray
Write-Host "  4. 重启手机生效" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Green

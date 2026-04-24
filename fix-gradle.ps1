# Fix Gradle Wrapper Script
$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "  Gradle Wrapper 修复工具" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 检查wrapper目录
$wrapperDir = "gradle\wrapper"
if (-not (Test-Path $wrapperDir)) {
    Write-Host "[1/4] 创建wrapper目录..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# 下载gradle-wrapper.jar
Write-Host "[2/4] 下载gradle-wrapper.jar..." -ForegroundColor Yellow
$wrapperJar = "$wrapperDir\gradle-wrapper.jar"
$wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"

try {
    # 尝试从GitHub下载
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar -UseBasicParsing -TimeoutSec 30
    Write-Host "✓ 从GitHub下载成功" -ForegroundColor Green
} catch {
    Write-Host "✗ GitHub下载失败，尝试国内镜像..." -ForegroundColor Red
    # 使用国内镜像
    $mirrorUrl = "https://mirrors.tuna.tsinghua.edu.cn/gradle/wrapper/gradle-wrapper.jar"
    try {
        Invoke-WebRequest -Uri $mirrorUrl -OutFile $wrapperJar -UseBasicParsing -TimeoutSec 30
        Write-Host "✓ 从清华镜像下载成功" -ForegroundColor Green
    } catch {
        Write-Host "✗ 下载失败，请检查网络连接" -ForegroundColor Red
        exit 1
    }
}

# 创建gradle-wrapper.properties
Write-Host "[3/4] 配置gradle-wrapper.properties..." -ForegroundColor Yellow
$wrapperProps = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
$wrapperProps | Out-File -FilePath "$wrapperDir\gradle-wrapper.properties" -Encoding UTF8
Write-Host "✓ 配置完成（使用腾讯云镜像）" -ForegroundColor Green

# 验证安装
Write-Host "[4/4] 验证Gradle安装..." -ForegroundColor Yellow
try {
    $output = & .\gradlew.bat --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Gradle验证成功！" -ForegroundColor Green
        Write-Host ""
        Write-Host $output
    } else {
        throw "Gradle验证失败"
    }
} catch {
    Write-Host "✗ Gradle验证失败" -ForegroundColor Red
    Write-Host "错误信息：$_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Gradle Wrapper 修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "现在可以运行构建命令了：" -ForegroundColor Yellow
Write-Host "  .\gradlew.bat assembleDebug" -ForegroundColor Cyan
Write-Host ""
pause

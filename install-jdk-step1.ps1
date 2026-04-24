# JDK 17 安装 - 第一步：下载
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装 - 第一步：下载" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$tempDir = "$env:TEMP\jdk_install_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

# 下载JDK
$jdkUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip"
$jdkZip = "$tempDir\jdk17.zip"

Write-Host "[1/3] 下载JDK 17..." -ForegroundColor Yellow
Write-Host "  URL: $jdkUrl" -ForegroundColor Gray

try {
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing -TimeoutSec 600
    $ProgressPreference = 'Continue'
    
    if (Test-Path $jdkZip) {
        $fileSize = (Get-Item $jdkZip).Length
        Write-Host "✓ 下载成功: $([math]::Round($fileSize/1MB, 2)) MB" -ForegroundColor Green
    } else {
        throw "下载文件不存在"
    }
} catch {
    Write-Host "✗ 下载失败: $_" -ForegroundColor Red
    exit 1
}

# 解压JDK
Write-Host ""
Write-Host "[2/3] 解压JDK..." -ForegroundColor Yellow
$jdkExtractPath = "C:\Program Files\Java"

try {
    if (-not (Test-Path $jdkExtractPath)) {
        New-Item -ItemType Directory -Path $jdkExtractPath -Force | Out-Null
    }
    
    Expand-Archive -Path $jdkZip -DestinationPath $jdkExtractPath -Force
    
    $jdkFolder = Get-ChildItem "$jdkExtractPath\jdk-17*" | Select-Object -First 1
    if ($jdkFolder) {
        Write-Host "✓ 解压完成: $($jdkFolder.Name)" -ForegroundColor Green
    } else {
        throw "无法找到解压后的JDK目录"
    }
} catch {
    Write-Host "✗ 解压失败: $_" -ForegroundColor Red
    exit 1
}

# 配置环境变量
Write-Host ""
Write-Host "[3/3] 配置环境变量..." -ForegroundColor Yellow

try {
    if ($jdkFolder) {
        $javaHome = $jdkFolder.FullName
        
        # 设置系统级环境变量
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")
        Write-Host "✓ JAVA_HOME = $javaHome" -ForegroundColor Green
        
        # 更新系统Path
        $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
        $newEntry = "%JAVA_HOME%\bin"
        
        if ($currentPath -notlike "*$newEntry*") {
            [Environment]::SetEnvironmentVariable("Path", "$currentPath;$newEntry", "Machine")
            Write-Host "✓ Path 已更新" -ForegroundColor Green
        }
    }
} catch {
    Write-Host "✗ 环境变量配置失败: $_" -ForegroundColor Red
}

# 验证
Write-Host ""
Write-Host "验证安装..." -ForegroundColor Yellow
$javaExe = "$env:JAVA_HOME\bin\java.exe"
if (Test-Path $javaExe) {
    & $javaExe -version 2>&1 | Select-Object -First 3 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
}

# 清理
Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
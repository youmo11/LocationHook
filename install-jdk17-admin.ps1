# JDK 17 安装脚本 - 管理员权限版
# 以管理员身份运行此脚本

param()

# 强制以管理员身份运行
if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "正在请求管理员权限..." -ForegroundColor Yellow
    $scriptPath = $MyInvocation.MyCommand.Path
    Start-Process powershell.exe -ArgumentList "-ExecutionPolicy Bypass -File `"$scriptPath`"" -Verb RunAs -Wait
    exit
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装 - 管理员模式" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 创建临时目录
$tempDir = "$env:TEMP\jdk_install_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

# 下载JDK
$jdkUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip"
$jdkZip = "$tempDir\jdk17.zip"

Write-Host "[1/4] 下载JDK 17..." -ForegroundColor Yellow
try {
    # 使用curl.exe下载（Windows 10+内置）
    $curlProcess = Start-Process -FilePath "curl.exe" -ArgumentList "-L", "-o", "$jdkZip", "$jdkUrl", "--max-time", "300", "-#" -Wait -PassThru -NoNewWindow
    
    if ($curlProcess.ExitCode -eq 0 -and (Test-Path $jdkZip)) {
        $fileSize = (Get-Item $jdkZip).Length
        if ($fileSize -gt 100MB) {
            Write-Host "✓ 下载成功: $([math]::Round($fileSize/1MB, 2)) MB" -ForegroundColor Green
        } else {
            throw "下载文件不完整"
        }
    } else {
        throw "curl下载失败，退出码: $($curlProcess.ExitCode)"
    }
} catch {
    Write-Host "✗ 下载失败: $_" -ForegroundColor Red
    Write-Host "  请检查网络连接或手动下载JDK 17" -ForegroundColor Yellow
    exit 1
}

# 解压JDK
Write-Host ""
Write-Host "[2/4] 解压JDK..." -ForegroundColor Yellow
$jdkExtractPath = "C:\Program Files\Java"

try {
    if (-not (Test-Path $jdkExtractPath)) {
        New-Item -ItemType Directory -Path $jdkExtractPath -Force | Out-Null
    }
    
    Expand-Archive -Path $jdkZip -DestinationPath $jdkExtractPath -Force
    
    # 获取JDK目录名
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
Write-Host "[3/4] 配置环境变量..." -ForegroundColor Yellow

try {
    if ($jdkFolder) {
        $javaHome = $jdkFolder.FullName
        
        # 设置JAVA_HOME（用户级别）
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "User")
        Write-Host "✓ JAVA_HOME 已设置: $javaHome" -ForegroundColor Green
        
        # 更新用户Path
        $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
        $newPathEntry = "%JAVA_HOME%\bin"
        
        if ($currentPath -notlike "*$newPathEntry*") {
            [Environment]::SetEnvironmentVariable("Path", "$currentPath;$newPathEntry", "User")
            Write-Host "✓ 用户Path已更新" -ForegroundColor Green
        } else {
            Write-Host "  用户Path已包含JAVA路径" -ForegroundColor Gray
        }
    } else {
        throw "找不到JDK目录"
    }
} catch {
    Write-Host "✗ 环境变量配置失败: $_" -ForegroundColor Red
    Write-Host "  请手动配置JAVA_HOME和Path" -ForegroundColor Yellow
}

# 验证安装
Write-Host ""
Write-Host "[4/4] 验证安装..." -ForegroundColor Yellow

# 刷新当前会话的环境变量
$env:JAVA_HOME = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
$env:Path = [Environment]::GetEnvironmentVariable("Path", "User")

# 测试java命令
$javaExe = "$env:JAVA_HOME\bin\java.exe"
if (Test-Path $javaExe) {
    try {
        $javaVersion = & $javaExe -version 2>&1
        if ($javaVersion -match "17\.") {
            Write-Host "✓ Java 17 验证成功!" -ForegroundColor Green
            $javaVersion | Select-Object -First 3 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
        } else {
            Write-Host "⚠ 警告: Java版本不是17" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "⚠ 警告: 无法执行java命令: $_" -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠ 警告: 找不到java.exe，需要重新启动PowerShell" -ForegroundColor Yellow
}

# 清理临时文件
Write-Host ""
Write-Host "清理临时文件..." -ForegroundColor Gray
if (Test-Path $tempDir) {
    Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  临时文件已清理" -ForegroundColor Gray
}

# 完成
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "安装路径: $env:JAVA_HOME" -ForegroundColor White
Write-Host "Java命令: $env:JAVA_HOME\bin\java.exe" -ForegroundColor White
Write-Host ""
Write-Host "请重新启动PowerShell以使用java命令" -ForegroundColor Yellow
Write-Host ""
Write-Host "下一步：安装Android SDK..." -ForegroundColor Cyan
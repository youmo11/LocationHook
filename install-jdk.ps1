# JDK 17 安装脚本
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 下载JDK 17 (Eclipse Adoptium)
$jdkUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip"
$jdkZip = "$env:TEMP\jdk17.zip"
$jdkExtractPath = "C:\Program Files\Java"

Write-Host "[1/5] 下载JDK 17..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing -TimeoutSec 300
    Write-Host "  下载完成!" -ForegroundColor Green
} catch {
    Write-Host "  下载失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host "[2/5] 解压JDK..." -ForegroundColor Yellow
if (-not (Test-Path $jdkExtractPath)) {
    New-Item -ItemType Directory -Path $jdkExtractPath -Force | Out-Null
}

Expand-Archive -Path $jdkZip -DestinationPath $jdkExtractPath -Force
Remove-Item $jdkZip
Write-Host "  解压完成!" -ForegroundColor Green

Write-Host "[3/5] 配置环境变量..." -ForegroundColor Yellow
$jdkPath = Get-ChildItem "$jdkExtractPath\jdk-*" | Select-Object -First 1
if ($jdkPath) {
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkPath.FullName, "Machine")
    $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    if ($currentPath -notlike "*JAVA_HOME*") {
        [Environment]::SetEnvironmentVariable("Path", "$currentPath;%JAVA_HOME%\bin", "Machine")
    }
    Write-Host "  环境变量配置完成!" -ForegroundColor Green
} else {
    Write-Host "  找不到JDK路径!" -ForegroundColor Red
    exit 1
}

Write-Host "[4/5] 验证JDK安装..." -ForegroundColor Yellow
$javaVersion = & "$($jdkPath.FullName)\bin\java.exe" -version 2>&1
if ($javaVersion -match "17\.") {
    Write-Host "  JDK 17 安装成功!" -ForegroundColor Green
} else {
    Write-Host "  JDK验证失败!" -ForegroundColor Red
}

Write-Host "[5/5] 清理临时文件..." -ForegroundColor Yellow
Remove-Item $jdkZip -ErrorAction SilentlyContinue
Write-Host "  清理完成!" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "安装路径: $($jdkPath.FullName)" -ForegroundColor White
Write-Host "JAVA_HOME: $($jdkPath.FullName)" -ForegroundColor White
Write-Host ""
Write-Host "请重新打开PowerShell或命令提示符以使用java命令" -ForegroundColor Yellow

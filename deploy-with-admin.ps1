# 管理员权限部署脚本 - 本地APK打包环境部署
# 运行方式: 右键→使用PowerShell运行 或 以管理员身份运行

param()

# 检查是否以管理员身份运行
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")

if (-not $isAdmin) {
    Write-Host "正在请求管理员权限..." -ForegroundColor Yellow
    # 尝试自提升
    $scriptPath = $MyInvocation.MyCommand.Path
    Start-Process PowerShell -Verb RunAs -ArgumentList "-ExecutionPolicy Bypass -File `"$scriptPath`"" -Wait
    exit
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  本地APK打包环境部署 - 管理员模式" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 创建日志文件
$logFile = "$PSScriptRoot\deploy-log.txt"
function Log-Message {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] $Message"
    Add-Content -Path $logFile -Value $logEntry
    Write-Host $Message -ForegroundColor $Color
}

Log-Message "开始部署本地APK打包环境" "Green"
Log-Message "管理员权限: 已获取" "Green"

# 步骤1: 安装JDK 17
Log-Message "步骤1: 安装JDK 17" "Yellow"

$jdkUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip"
$jdkZip = "$env:TEMP\jdk17.zip"
$jdkExtractPath = "C:\Program Files\Java"

if (Test-Path "$jdkExtractPath\jdk-17*") {
    Log-Message "JDK 17 已安装，跳过" "Green"
} else {
    Log-Message "下载JDK 17..." "Yellow"
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing -TimeoutSec 300
        Log-Message "JDK下载完成" "Green"
        
        Log-Message "解压JDK..." "Yellow"
        if (-not (Test-Path $jdkExtractPath)) {
            New-Item -ItemType Directory -Path $jdkExtractPath -Force | Out-Null
        }
        Expand-Archive -Path $jdkZip -DestinationPath $jdkExtractPath -Force
        Remove-Item $jdkZip
        Log-Message "JDK解压完成" "Green"
        
        # 配置环境变量
        $jdkFolder = Get-ChildItem "$jdkExtractPath\jdk-17*" | Select-Object -First 1
        if ($jdkFolder) {
            [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkFolder.FullName, "Machine")
            $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
            if ($currentPath -notlike "*JAVA_HOME*") {
                [Environment]::SetEnvironmentVariable("Path", "$currentPath;%JAVA_HOME%\bin", "Machine")
            }
            Log-Message "环境变量配置完成" "Green"
        }
        
    } catch {
        Log-Message "JDK安装失败: $_" "Red"
    }
}

Log-Message "步骤1完成" "Green"
Write-Host ""

# 后续步骤会继续...
Log-Message "部署脚本执行中，继续下一步..." "Cyan"
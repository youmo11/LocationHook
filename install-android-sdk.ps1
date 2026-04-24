# Android SDK 36 安装脚本
# Android 16 (API 36) 开发套件

param()

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Android SDK 36 安装" -ForegroundColor Cyan
Write-Host "  Android 16 (API 36) 开发套件" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查JAVA_HOME
$javaHome = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
if (-not $javaHome) {
    Write-Host "✗ 错误: JAVA_HOME 未设置" -ForegroundColor Red
    Write-Host "  请先安装JDK 17并配置环境变量" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path "$javaHome\bin\java.exe")) {
    Write-Host "✗ 错误: 找不到java.exe在 $javaHome" -ForegroundColor Red
    exit 1
}

Write-Host "✓ JAVA_HOME: $javaHome" -ForegroundColor Green

# Android SDK 下载配置
$androidSdkUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$androidSdkZip = "$env:TEMP\android_sdk_tools.zip"
$androidSdkPath = "C:\Android\Sdk"

Write-Host ""
Write-Host "[1/4] 下载 Android SDK 命令行工具..." -ForegroundColor Yellow

try {
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $androidSdkUrl -OutFile $androidSdkZip -UseBasicParsing -TimeoutSec 600
    $ProgressPreference = 'Continue'
    
    if (Test-Path $androidSdkZip) {
        $fileSize = (Get-Item $androidSdkZip).Length
        Write-Host "✓ 下载成功: $([math]::Round($fileSize/1MB, 2)) MB" -ForegroundColor Green
    } else {
        throw "下载文件不存在"
    }
} catch {
    Write-Host "✗ 下载失败: $_" -ForegroundColor Red
    Write-Host "  请检查网络连接或手动下载Android SDK" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "[2/4] 解压 Android SDK..." -ForegroundColor Yellow

try {
    if (-not (Test-Path $androidSdkPath)) {
        New-Item -ItemType Directory -Path $androidSdkPath -Force | Out-Null
    }
    
    Expand-Archive -Path $androidSdkZip -DestinationPath "$androidSdkPath\cmdline-tools" -Force
    
    # 重命名目录（Android SDK要求特定的目录结构）
    $toolsDir = Get-ChildItem "$androidSdkPath\cmdline-tools" | Select-Object -First 1
    if ($toolsDir) {
        Rename-Item $toolsDir.FullName -NewName "latest" -Force
        Write-Host "✓ 解压完成: $androidSdkPath" -ForegroundColor Green
    } else {
        throw "无法找到解压后的SDK目录"
    }
} catch {
    Write-Host "✗ 解压失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/4] 配置 Android SDK 环境变量..." -ForegroundColor Yellow

try {
    # 设置ANDROID_HOME
    [Environment]::SetEnvironmentVariable("ANDROID_HOME", $androidSdkPath, "User")
    Write-Host "✓ ANDROID_HOME = $androidSdkPath" -ForegroundColor Green
    
    # 更新Path
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $androidEntries = @(
        "$androidSdkPath\cmdline-tools\latest\bin",
        "$androidSdkPath\platform-tools",
        "$androidSdkPath\emulator"
    )
    
    foreach ($entry in $androidEntries) {
        if ($userPath -notlike "*$entry*") {
            $userPath += ";$entry"
        }
    }
    
    [Environment]::SetEnvironmentVariable("Path", $userPath, "User")
    Write-Host "✓ Path 已更新" -ForegroundColor Green
    
} catch {
    Write-Host "✗ 环境变量配置失败: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "[4/4] 验证 Android SDK..." -ForegroundColor Yellow

# 刷新当前会话的环境变量
$env:ANDROID_HOME = $androidSdkPath
$env:Path = [Environment]::GetEnvironmentVariable("Path", "User")

# 检查关键工具
$toolsToCheck = @(
    @{ Name = "sdkmanager"; Path = "$androidSdkPath\cmdline-tools\latest\bin\sdkmanager.bat" },
    @{ Name = "adb"; Path = "$androidSdkPath\platform-tools\adb.exe" }
)

$allToolsFound = $true
foreach ($tool in $toolsToCheck) {
    if (Test-Path $tool.Path) {
        Write-Host "✓ 找到 $($tool.Name): $($tool.Path)" -ForegroundColor Green
    } else {
        Write-Host "✗ 找不到 $($tool.Name)" -ForegroundColor Red
        $allToolsFound = $false
    }
}

# 清理
Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($allToolsFound) {
    Write-Host "  Android SDK 配置成功！" -ForegroundColor Green
} else {
    Write-Host "  Android SDK 配置完成（部分工具缺失）" -ForegroundColor Yellow
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "SDK路径: $androidSdkPath" -ForegroundColor White
Write-Host ""
Write-Host "下一步：安装Android平台工具（platform-tools）..." -ForegroundColor Cyan
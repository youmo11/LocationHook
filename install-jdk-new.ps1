# JDK 17 安装脚本 - 方案B
# 以管理员权限运行

param()

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装 - 方案B" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
if (-not $isAdmin) {
    Write-Host "错误：需要管理员权限！" -ForegroundColor Red
    Write-Host "请右键点击PowerShell选择'以管理员身份运行'" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ 管理员权限已确认" -ForegroundColor Green
Write-Host ""

# JDK文件路径
$jdkZip = "$env:TEMP\jdk17.zip"
$jdkExtractPath = "C:\Program Files\Java"

# 检查下载文件
if (-not (Test-Path $jdkZip)) {
    Write-Host "错误：找不到JDK下载文件 ($jdkZip)" -ForegroundColor Red
    Write-Host "请先下载JDK 17到上述路径" -ForegroundColor Yellow
    exit 1
}

$fileSize = (Get-Item $jdkZip).Length
Write-Host "✓ 找到JDK压缩包: $([math]::Round($fileSize/1MB, 2)) MB" -ForegroundColor Green

# 步骤1: 解压JDK
Write-Host ""
Write-Host "步骤1/3: 解压JDK 17..." -ForegroundColor Yellow

if (-not (Test-Path $jdkExtractPath)) {
    New-Item -ItemType Directory -Path $jdkExtractPath -Force | Out-Null
}

try {
    Expand-Archive -Path $jdkZip -DestinationPath $jdkExtractPath -Force
    Write-Host "✓ 解压完成" -ForegroundColor Green
    
    # 获取JDK目录名
    $jdkFolder = Get-ChildItem "$jdkExtractPath\jdk-17*" | Select-Object -First 1
    if ($jdkFolder) {
        Write-Host "  JDK路径: $($jdkFolder.FullName)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ 解压失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤2: 配置环境变量
Write-Host ""
Write-Host "步骤2/3: 配置环境变量..." -ForegroundColor Yellow

try {
    if ($jdkFolder) {
        # 设置JAVA_HOME
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkFolder.FullName, "Machine")
        Write-Host "✓ JAVA_HOME 已设置: $($jdkFolder.FullName)" -ForegroundColor Green
        
        # 更新Path
        $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
        $newPathEntry = "%JAVA_HOME%\bin"
        
        if ($currentPath -notlike "*$newPathEntry*") {
            [Environment]::SetEnvironmentVariable("Path", "$currentPath;$newPathEntry", "Machine")
            Write-Host "✓ Path 已更新" -ForegroundColor Green
        } else {
            Write-Host "  Path 已包含JAVA路径，跳过" -ForegroundColor Gray
        }
    } else {
        Write-Host "✗ 找不到JDK目录" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 环境变量配置失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤3: 验证安装
Write-Host ""
Write-Host "步骤3/3: 验证安装..." -ForegroundColor Yellow

# 需要刷新环境变量才能使用新设置的变量
$env:JAVA_HOME = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
$env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine")

# 测试java命令
$javaExe = "$env:JAVA_HOME\bin\java.exe"
if (Test-Path $javaExe) {
    try {
        $javaVersion = & $javaExe -version 2>&1
        if ($javaVersion -match "17\.") {
            Write-Host "✓ Java 17 验证成功!" -ForegroundColor Green
            Write-Host "  版本信息:" -ForegroundColor Gray
            $javaVersion | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
        } else {
            Write-Host "⚠ 警告: Java版本可能不是17" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "⚠ 警告: 无法执行java命令: $_" -ForegroundColor Yellow
    }
} else {
    Write-Host "✗ 找不到java.exe: $javaExe" -ForegroundColor Red
}

# 清理临时文件
Write-Host ""
Write-Host "清理临时文件..." -ForegroundColor Gray
if (Test-Path $jdkZip) {
    Remove-Item $jdkZip -Force
    Write-Host "  已删除临时文件" -ForegroundColor Gray
}

# 完成
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 部署完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "安装路径: $env:JAVA_HOME" -ForegroundColor White
Write-Host "Java命令: $env:JAVA_HOME\bin\java.exe" -ForegroundColor White
Write-Host ""
Write-Host "下一步：安装Android SDK和Gradle..." -ForegroundColor Cyan

# 暂停以便查看结果
# Read-Host "按回车键继续..."
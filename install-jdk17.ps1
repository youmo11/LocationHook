# JDK 17 安装脚本
# 使用Eclipse Adoptium / Microsoft Build of OpenJDK

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  JDK 17 安装脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 尝试多个下载源
$downloadSources = @(
    "https://aka.ms/download-jdk/microsoft-jdk-17.0.9-windows-x64.zip",
    "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip",
    "https://download.java.net/openjdk/jdk17/ri/openjdk-17+35_windows-x64_bin.zip"
)

$jdkZip = "$env:TEMP\jdk17_install.zip"
$jdkExtractPath = "C:\Program Files\Java"

# 尝试每个下载源
$downloadSuccess = $false
for ($i = 0; $i -lt $downloadSources.Count; $i++) {
    $url = $downloadSources[$i]
    Write-Host "尝试下载源 $($i+1)/$($downloadSources.Count): $url" -ForegroundColor Yellow
    
    try {
        # 使用curl.exe（Windows 10+自带）
        $curlArgs = "-L `"$url`" -o `"$jdkZip`" --max-time 120 -s"
        $process = Start-Process -FilePath "curl.exe" -ArgumentList $curlArgs -Wait -NoNewWindow -PassThru
        
        if ($process.ExitCode -eq 0 -and (Test-Path $jdkZip)) {
            $fileSize = (Get-Item $jdkZip).Length
            if ($fileSize -gt 100MB) {
                Write-Host "✓ 下载成功: $([math]::Round($fileSize/1MB, 2)) MB" -ForegroundColor Green
                $downloadSuccess = $true
                break
            } else {
                Write-Host "⚠ 文件太小，可能下载不完整" -ForegroundColor Yellow
                Remove-Item $jdkZip -Force -ErrorAction SilentlyContinue
            }
        } else {
            Write-Host "✗ 下载失败 (ExitCode: $($process.ExitCode))" -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ 下载异常: $_" -ForegroundColor Red
    }
    
    if (-not $downloadSuccess -and $i -lt $downloadSources.Count - 1) {
        Write-Host "等待3秒后尝试下一个源..." -ForegroundColor Gray
        Start-Sleep -Seconds 3
    }
}

if (-not $downloadSuccess) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  所有下载源均失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "可能原因：" -ForegroundColor Yellow
    Write-Host "- 网络连接问题" -ForegroundColor Gray
    Write-Host "- 防火墙/代理限制" -ForegroundColor Gray
    Write-Host "- 所有下载源已失效" -ForegroundColor Gray
    Write-Host ""
    Write-Host "建议：" -ForegroundColor Cyan
    Write-Host "1. 检查网络连接和代理设置" -ForegroundColor White
    Write-Host "2. 尝试手动下载JDK 17安装包" -ForegroundColor White
    Write-Host "3. 使用GitHub Actions自动构建方案" -ForegroundColor White
    Write-Host ""
    exit 1
}

# 步骤2: 解压JDK
Write-Host ""
Write-Host "步骤2/4: 解压JDK..." -ForegroundColor Yellow

if (-not (Test-Path $jdkExtractPath)) {
    try {
        New-Item -ItemType Directory -Path $jdkExtractPath -Force | Out-Null
        Write-Host "  创建目录: $jdkExtractPath" -ForegroundColor Gray
    } catch {
        Write-Host "✗ 无法创建目录 $jdkExtractPath : $_" -ForegroundColor Red
        Write-Host "  尝试使用用户目录..." -ForegroundColor Yellow
        $jdkExtractPath = "$env:USERPROFILE\Java"
        New-Item -ItemType Directory -Path $jdkExtractPath -Force | Out-Null
    }
}

try {
    Write-Host "  正在解压到 $jdkExtractPath ..." -ForegroundColor Gray
    Expand-Archive -Path $jdkZip -DestinationPath $jdkExtractPath -Force
    Write-Host "✓ 解压完成" -ForegroundColor Green
    
    # 获取JDK目录名
    $jdkFolder = Get-ChildItem "$jdkExtractPath\jdk-17*" | Select-Object -First 1
    if ($jdkFolder) {
        Write-Host "  JDK目录: $($jdkFolder.Name)" -ForegroundColor Gray
    } else {
        Write-Host "⚠ 警告: 无法找到解压后的JDK目录" -ForegroundColor Yellow
    }
} catch {
    Write-Host "✗ 解压失败: $_" -ForegroundColor Red
    exit 1
}

# 步骤3: 配置环境变量
Write-Host ""
Write-Host "步骤3/4: 配置环境变量..." -ForegroundColor Yellow

try {
    if ($jdkFolder) {
        # 设置JAVA_HOME
        $javaHome = $jdkFolder.FullName
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")
        Write-Host "✓ JAVA_HOME = $javaHome" -ForegroundColor Green
        
        # 更新Path
        $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
        $newPathEntry = "%JAVA_HOME%\bin"
        
        if ($currentPath -notlike "*JAVA_HOME*") {
            [Environment]::SetEnvironmentVariable("Path", "$currentPath;$newPathEntry", "Machine")
            Write-Host "✓ Path 已更新" -ForegroundColor Green
        } else {
            Write-Host "  Path 已包含JAVA路径" -ForegroundColor Gray
        }
    } else {
        Write-Host "✗ 找不到JDK目录" -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 环境变量配置失败: $_" -ForegroundColor Red
}

# 步骤4: 验证安装
Write-Host ""
Write-Host "步骤4/4: 验证安装..." -ForegroundColor Yellow

# 刷新环境变量
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
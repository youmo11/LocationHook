# 配置Java 17环境变量
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  配置Java 17环境变量" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$javaPath = "C:\Users\Administrator\.jdks\jbr-17.0.14"

# 验证Java路径
if (-not (Test-Path "$javaPath\bin\java.exe")) {
    Write-Host "✗ 错误: 找不到java.exe在 $javaPath" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 找到Java 17: $javaPath" -ForegroundColor Green

# 配置用户级环境变量（无需管理员权限）
Write-Host ""
Write-Host "[1/2] 设置JAVA_HOME..." -ForegroundColor Yellow

[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, "User")
Write-Host "✓ JAVA_HOME = $javaPath" -ForegroundColor Green

Write-Host ""
Write-Host "[2/2] 更新Path..." -ForegroundColor Yellow

$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
$newEntry = "%JAVA_HOME%\bin"

if ($userPath -notlike "*$newEntry*") {
    [Environment]::SetEnvironmentVariable("Path", "$userPath;$newEntry", "User")
    Write-Host "✓ Path已添加: $newEntry" -ForegroundColor Green
} else {
    Write-Host "  Path已包含JAVA路径，跳过" -ForegroundColor Gray
}

# 设置当前会话的环境变量
$env:JAVA_HOME = $javaPath
$env:Path = "$env:Path;$javaPath\bin"

# 验证
Write-Host ""
Write-Host "验证安装..." -ForegroundColor Yellow

$javaVersion = & "$javaPath\bin\java.exe" -version 2>&1
if ($javaVersion -match "17\.") {
    Write-Host "✓ Java 17验证成功!" -ForegroundColor Green
    $javaVersion | Select-Object -First 3 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
} else {
    Write-Host "⚠ 警告: 无法验证Java版本" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Java 17环境配置完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Java路径: $javaPath" -ForegroundColor White
Write-Host "Java命令: $javaPath\bin\java.exe" -ForegroundColor White
Write-Host ""
Write-Host "提示: 请重新打开PowerShell以使环境变量生效" -ForegroundColor Yellow
Write-Host ""
Write-Host "下一步: 安装Android SDK..." -ForegroundColor Cyan
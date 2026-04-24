# 自动修复环境并构建APK
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LocationHook 自动修复与构建脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查并安装Java
Write-Host "[步骤1/7] 检查Java环境..." -ForegroundColor Yellow
$javaPath = "C:\Program Files\OpenJDK\jdk-17.0.9+9"
if (-not (Test-Path $javaPath)) {
    Write-Host "  未找到Java，正在从清华镜像下载..." -ForegroundColor Red
    try {
        $url = "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/x64/windows/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.zip"
        Invoke-WebRequest -Uri $url -OutFile "$env:TEMP\openjdk.zip" -UseBasicParsing -TimeoutSec 300
        Expand-Archive -Path "$env:TEMP\openjdk.zip" -DestinationPath "C:\Program Files\OpenJDK" -Force
        Remove-Item "$env:TEMP\openjdk.zip"
        Write-Host "  Java安装完成！" -ForegroundColor Green
    } catch {
        Write-Host "  Java下载失败，尝试备用方案..." -ForegroundColor Red
    }
}

# 设置Java环境变量
$env:JAVA_HOME = $javaPath
$env:PATH = "$javaPath\bin;$env:PATH"
Write-Host "  Java路径: $javaPath" -ForegroundColor Green

# 2. 配置Gradle国内镜像
Write-Host "[步骤2/7] 配置Gradle国内镜像..." -ForegroundColor Yellow
$gradleDir = "gradle\wrapper"
if (-not (Test-Path $gradleDir)) {
    New-Item -ItemType Directory -Path $gradleDir -Force | Out-Null
}

$wrapperProps = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
$wrapperProps | Out-File -FilePath "$gradleDir\gradle-wrapper.properties" -Encoding UTF8 -Force
Write-Host "  Gradle镜像配置完成（腾讯云）" -ForegroundColor Green

# 3. 配置项目build.gradle国内镜像
Write-Host "[步骤3/7] 配置项目国内镜像..." -ForegroundColor Yellow
$buildGradle = @"
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.locationhook.module'
    compileSdk 36

    defaultConfig {
        applicationId "com.locationhook.module"
        minSdk 26
        targetSdk 36
        versionCode 1
        versionName "1.0.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly 'de.robv.android.xposed:api:82'
    compileOnly 'de.robv.android.xposed:api:82:sources'
    implementation 'com.amap.api:map3d:latest.integration'
    implementation 'com.amap.api:search:latest.integration'
    implementation 'com.amap.api:location:latest.integration'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.orhanobut:logger:2.2.0'
}

// 配置国内镜像
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        maven { url 'https://mirrors.tuna.tsinghua.edu.cn/maven/' }
        maven { url 'https://mirrors.cloud.tencent.com/maven/' }
        mavenCentral()
        google()
        jcenter()
    }
}

// 构建配置
gradle.projectsEvaluated {
    tasks.withType(JavaCompile) {
        options.compilerArgs << "-Xmaxerrs" << "500"
    }
}
"@
$buildGradle | Out-File -FilePath "build.gradle" -Encoding UTF8 -Force
Write-Host "  build.gradle配置完成（已配置国内镜像）" -ForegroundColor Green

# 4. 配置settings.gradle
Write-Host "[步骤4/7] 配置settings.gradle..." -ForegroundColor Yellow
$settingsGradle = @"
pluginManagement {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://mirrors.tuna.tsinghua.edu.cn/maven/' }
        maven { url 'https://mirrors.cloud.tencent.com/maven/' }
        mavenCentral()
        google()
        jcenter()
    }
}
rootProject.name = "LocationHook"
include ':app'
"@
$settingsGradle | Out-File -FilePath "settings.gradle" -Encoding UTF8 -Force
Write-Host "  settings.gradle配置完成" -ForegroundColor Green

# 5. 下载并配置Gradle Wrapper
Write-Host "[步骤5/7] 下载并配置Gradle Wrapper..." -ForegroundColor Yellow
$wrapperDir = "gradle\wrapper"
if (-not (Test-Path $wrapperDir)) {
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# 检查gradle-wrapper.jar是否存在
$wrapperJar = "$wrapperDir\gradle-wrapper.jar"
if (-not (Test-Path $wrapperJar)) {
    Write-Host "  正在下载gradle-wrapper.jar..." -ForegroundColor Yellow
    $wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar"
    try {
        Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar -UseBasicParsing -TimeoutSec 120
        Write-Host "  下载完成！" -ForegroundColor Green
    } catch {
        Write-Host "  GitHub下载失败，尝试国内镜像..." -ForegroundColor Red
        # 使用备用镜像
        $mirrorUrl = "https://mirrors.tuna.tsinghua.edu.cn/gradle/wrapper/gradle-wrapper.jar"
        try {
            Invoke-WebRequest -Uri $mirrorUrl -OutFile $wrapperJar -UseBasicParsing -TimeoutSec 120
            Write-Host "  从清华镜像下载成功！" -ForegroundColor Green
        } catch {
            Write-Host "  下载失败，请检查网络连接" -ForegroundColor Red
            exit 1
        }
    }
}

# 创建gradle-wrapper.properties
$wrapperProps = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
$wrapperProps | Out-File -FilePath "$wrapperDir\gradle-wrapper.properties" -Encoding UTF8 -Force
Write-Host "  Gradle Wrapper配置完成（腾讯云镜像）" -ForegroundColor Green

# 6. 开始构建
Write-Host "[步骤6/7] 开始构建APK..." -ForegroundColor Yellow
Write-Host "  这可能需要5-10分钟，请耐心等待..." -ForegroundColor Yellow
Write-Host ""

# 清理旧构建
Write-Host "  [1/3] 清理旧构建..." -ForegroundColor Gray
& .\gradlew.bat clean --quiet 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "    清理失败，继续尝试构建..." -ForegroundColor Red
}

# 构建Debug APK
Write-Host "  [2/3] 构建Debug APK..." -ForegroundColor Gray
& .\gradlew.bat assembleDebug --parallel --build-cache --configure-on-demand --no-daemon --quiet 2>&1 | ForEach-Object {
    if ($_ -match "BUILD|FAILED|SUCCESS|ERROR") {
        Write-Host "    $_" -ForegroundColor Gray
    }
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "  ❌ 构建失败！尝试使用备用方案..." -ForegroundColor Red
    Write-Host "  可能是网络问题，正在重试..." -ForegroundColor Yellow
    & .\gradlew.bat clean assembleDebug --no-daemon 2>&1 | ForEach-Object {
        if ($_ -match "BUILD|FAILED|SUCCESS|ERROR") {
            Write-Host "    $_" -ForegroundColor Gray
        }
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "  ❌ 构建失败！请查看错误信息。" -ForegroundColor Red
        exit 1
    }
}

# 7. 验证构建结果
Write-Host "  [3/3] 验证构建结果..." -ForegroundColor Gray
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host ""
    Write-Host "  ❌ 未找到生成的APK文件！" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length
$apkSizeMB = [math]::Round($apkSize / 1MB, 2)

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ APK构建成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📦 文件信息：" -ForegroundColor Yellow
Write-Host "  位置: $apkPath" -ForegroundColor White
Write-Host "  大小: $apkSize 字节 ($apkSizeMB MB)" -ForegroundColor White
Write-Host ""
Write-Host "📱 安装方法：" -ForegroundColor Yellow
Write-Host "  1. 将APK传输到手机" -ForegroundColor Gray
Write-Host "  2. 在手机上安装APK" -ForegroundColor Gray
Write-Host "  3. 在LSPosed中启用模块" -ForegroundColor Gray
Write-Host "  4. 重启手机" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Green

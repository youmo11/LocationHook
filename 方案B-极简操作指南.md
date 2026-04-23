# 方案B：检查配置 + 手动打包（极简版）

## 第一步：确认项目文件完整（30秒）
打开文件夹检查：
```
C:\Users\Administrator\.openclaw\workspace\projects\LocationHook\
```
确认有以下文件夹：
- ✅ `app/src/main/java/` - Java代码
- ✅ `app/src/main/res/` - 界面资源
- ✅ `app/build.gradle` - 构建配置

## 第二步：配置高德Key（可选，1分钟）
如果需要地图功能：
1. 在项目根目录创建/编辑 `local.properties`
2. 添加内容：
   ```properties
   AMAP_API_KEY=你的高德地图Key
   ```
3. Key申请：https://lbs.amap.com/（免费，包名填`com.locationhook.module`）

## 第三步：用Android Studio打包（5分钟）
### 3.1 打开项目
1. 打开Android Studio
2. 点击 **Open an existing project**
3. 选择：`C:卷ers/Administrator/.openclaw/workspace/projects/LocationHook`
4. 点击 **OK**

### 3.2 等待同步
- 等待Gradle同步完成（3-5分钟）
- 右下角显示进度，完成后显示绿色✓

### 3.3 开始打包
1. 点击菜单栏：**Build → Build Bundle(s)/APK(s) → Build APK(s)**
2. 等待2-3分钟
3. 成功后右下角弹出提示，点击 **locate**

### 3.4 找到APK
APK文件位置：
```
app\build\outputs\apk\debug\app-debug.apk
```

## 第四步：安装使用（2分钟）
1. 把APK传到手机安装
2. 打开LSPosed → 模块 → 启用LocationHook
3. 选择作用域（勾选"系统框架"+需要Hook的应用）
4. 重启手机
5. 打开APP，地图上选位置，开启模拟开关

---

## ❗ 如果环境没配置（没装Android Studio）

**最快的替代方案：GitHub Actions自动打包**

### 5分钟搞定，无需安装任何软件：

1. 访问 https://github.com/new
2. 创建新仓库，名称：`LocationHook`
3. 在电脑上把项目文件夹压缩成zip
4. 回到GitHub仓库，点击 **Add file → Upload files**
5. 上传zip文件，点击 **Commit changes**
6. 等待5分钟，点击 **Actions** 标签
7. 看到绿色✓后，下载 **Artifacts** 中的 `app-debug`
8. 解压得到APK

---

## 🆘 遇到问题怎么办？

如果在打包过程中遇到任何错误：
1. 把错误信息截图或复制给我
2. 我会帮你分析问题原因
3. 提供具体的解决方案

随时找我！😊

# 🚀 GitHub自动打包操作指南

## 概述
这是最简单的方式，无需安装任何开发环境，10分钟获得APK。

---

## 📋 操作步骤（总共10分钟）

### 第1步：注册GitHub账号（2分钟）
1. 打开 https://github.com/signup
2. 输入邮箱、设置密码、用户名
3. 验证邮箱，完成注册

### 第2步：创建仓库（1分钟）
1. 登录GitHub后，点击右上角 **+** → **New repository**
2. Repository name 填：`LocationHook`
3. 选择 **Public** 或 **Private** 都可以
4. 点击 **Create repository**

### 第3步：上传代码（3分钟）
**方法一：浏览器直接上传（最简单）**
1. 在项目文件夹 `C:\Users\Administrator\.openclaw\workspace\projects\LocationHook\`
2. 把所有文件和文件夹压缩成一个zip文件
3. 回到GitHub仓库页面
4. 点击 **Add file → Upload files**
5. 拖拽或选择zip文件上传
6. 点击 **Commit changes**

**方法二：Git命令（推荐）**
```bash
cd C:\Users\Administrator\.openclaw\workspace\projects\LocationHook
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/你的用户名/LocationHook.git
git push -u origin master
```

### 第4步：等待自动编译（5分钟）
1. 上传完成后，点击仓库顶部的 **Actions** 标签
2. 你会看到一个工作流正在运行（黄色圆圈）
3. 等待5-10分钟，直到变成绿色✓
4. 点击最新的运行记录
5. 滚动到底部，在 **Artifacts** 区域下载 `app-debug`

### 第5步：下载APK
1. 点击 `app-debug` 下载zip文件
2. 解压后得到 `app-debug.apk`
3. 拷贝到手机安装即可

---

## ✅ 安装使用步骤
1. 安装APK到手机
2. 打开LSPosed → 模块 → 启用LocationHook
3. 勾选作用域（系统框架+需要Hook的应用）
4. 重启手机
5. 打开LocationHook APP，地图上选位置，开启开关
6. 其他APP就会获取到模拟的位置

---

## ❓ 常见问题

### Q: Actions运行失败
- 检查代码是否完整上传
- 查看具体的错误日志

### Q: 下载的APK安装不了
- 检查是否已安装旧版本，先卸载再安装

### Q: 模块不生效
- 检查LSPosed中是否启用了模块
- 检查是否勾选了"系统框架"
- 是否重启了手机

---

## 📞 需要帮助？
如果在任何步骤遇到问题，把**具体的错误信息**或**截图**发给我，我会帮你解决！

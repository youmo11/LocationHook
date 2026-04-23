# 🚀 GitHub Actions 自动打包

无需本地Android环境，直接通过GitHub自动编译APK！

## 使用方法

### 步骤1：创建GitHub仓库

1. 访问 [GitHub](https://github.com) 登录你的账号
2. 点击右上角 **+** → **New repository**
3. 仓库名称填 `LocationHook`
4. 选择 **Public**（公开）或 **Private**（私有）
5. 点击 **Create repository**

### 步骤2：上传代码到GitHub

在本地项目目录下执行：

```bash
# 进入项目目录
cd C:\Users\Administrator\.openclaw\workspace\projects\LocationHook

# 初始化Git仓库
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit"

# 关联远程仓库（替换为你的仓库地址）
git remote add origin https://github.com/你的用户名/LocationHook.git

# 推送到GitHub
git push -u origin master
```

### 步骤3：触发自动打包

上传代码后，GitHub Actions会自动开始编译：

1. 打开你的GitHub仓库页面
2. 点击上方的 **Actions** 标签
3. 你会看到正在运行的工作流 **Build APK**
4. 等待约5-10分钟

### 步骤4：下载APK

编译完成后：

1. 在Actions页面点击最新的运行记录
2. 滚动到底部的 **Artifacts** 区域
3. 下载 `app-debug` 或 `app-release`
4. 解压下载的zip文件，里面就是APK

---

## ⚙️ 配置说明

### 配置高德地图Key

在GitHub仓库中设置Secret：

1. 打开仓库 → **Settings** → **Secrets and variables** → **Actions**
2. 点击 **New repository secret**
3. Name填 `AMAP_API_KEY`
4. Value填你的高德地图API Key
5. 修改 `.github/workflows/build.yml`，添加：

```yaml
    - name: Setup AMap Key
      run: |
        echo "AMAP_API_KEY=${{ secrets.AMAP_API_KEY }}" >> local.properties
```

---

## ❓ 常见问题

### Q: Actions运行失败
查看具体的错误日志，常见原因：
- 代码有语法错误
- 缺少必要的文件
- 依赖下载失败（网络问题）

### Q: 编译成功但APK运行崩溃
可能是：
- 高德地图Key未配置
- 缺少必要的权限
- 签名问题

### Q: 如何更新代码重新打包
```bash
git add .
git commit -m "更新说明"
git push
```
推送后Actions会自动重新编译。

---

## 📝 参考链接

- [GitHub Actions 文档](https://docs.github.com/cn/actions)
- [高德地图开发者平台](https://lbs.amap.com/)
- [LSPosed GitHub](https://github.com/LSPosed/LSPosed)

---

**现在就开始吧！上传代码，5分钟后就能拿到APK！** 🚀

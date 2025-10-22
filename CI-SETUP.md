# GitHub Actions CI/CD 设置指南

## 已创建的文件

### 1. 主要CI配置文件
- `.github/workflows/ci.yml` - 主要的构建和测试流程
- `.github/workflows/code-quality.yml` - 代码质量检查
- `.github/workflows/deploy.yml` - 部署流程

### 2. 测试环境配置
- `sky-server/src/main/resources/application-test.yml` - 测试环境数据库配置

## 工作流说明

### CI流程 (ci.yml)
1. **触发条件**: 推送到main/develop分支或创建PR
2. **环境**: Ubuntu + Java 8 + MySQL 8.0
3. **步骤**:
   - 检出代码
   - 设置Java环境
   - 缓存Maven依赖
   - 构建项目
   - 等待数据库启动
   - 运行测试
   - 生成测试报告
   - 打包应用
   - 上传构建产物

### 代码质量检查 (code-quality.yml)
1. **触发条件**: 推送到main/develop分支或创建PR
2. **功能**:
   - 代码格式检查 (Checkstyle)
   - 静态代码分析 (SpotBugs)
   - 代码覆盖率报告 (JaCoCo)
   - 上传覆盖率到Codecov

### 部署流程 (deploy.yml)
1. **触发条件**: 推送到main分支
2. **功能**:
   - 构建应用
   - 创建部署包
   - 上传部署产物

## 如何使用

### 1. 提交代码到GitHub
```bash
git add .
git commit -m "Add GitHub Actions CI/CD"
git push origin main
```

### 2. 查看CI状态
- 进入GitHub仓库页面
- 点击 "Actions" 标签
- 查看工作流运行状态

### 3. 查看测试报告
- 在Actions页面点击具体的运行记录
- 查看各个步骤的执行结果
- 下载构建产物

## 配置说明

### 数据库配置
- 测试环境使用MySQL 8.0
- 数据库名: sky_take_out
- 用户名: sky
- 密码: sky123

### Maven配置
- 使用Maven缓存加速构建
- 支持多模块项目构建
- 自动生成测试报告

## 故障排除

### 常见问题
1. **数据库连接失败**: 检查MySQL服务是否正常启动
2. **测试失败**: 检查测试环境配置是否正确
3. **构建失败**: 检查Maven依赖是否正确

### 调试方法
1. 查看Actions日志
2. 检查本地环境是否与CI环境一致
3. 验证配置文件是否正确

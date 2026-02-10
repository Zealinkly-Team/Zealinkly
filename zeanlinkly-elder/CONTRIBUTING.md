# 贡献指南

感谢你对 Zealinkly 项目的兴趣！我们欢迎各种形式的贡献。

## 📋 贡献流程

### 1. Fork 项目
```bash
# Fork 本仓库到你的 GitHub 账户
```

### 2. Clone 仓库
```bash
git clone https://github.com/YOUR_USERNAME/Zealinkly.git
cd zealinkly-elder
```

### 3. 创建分支
```bash
# 基于 main 分支创建新分支
git checkout -b feature/your-feature-name

# 或修复 Bug
git checkout -b bugfix/your-bugfix-name
```

### 4. 进行修改
- 遵循项目代码规范
- 添加必要的注释和文档
- 确保代码编译通过
- 添加单元测试（如适用）

### 5. 提交修改
```bash
git add .
git commit -m "feat: 添加新功能

- 详细描述修改内容
- 可以包含多行说明
- 遵循 Commit 规范"
```

### 6. Push 到你的分支
```bash
git push origin feature/your-feature-name
```

### 7. 创建 Pull Request
- 在 GitHub 上创建 PR
- 填写完整的 PR 描述
- 等待代码审查

## 📝 Commit 规范

我们使用 Conventional Commits 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type
- **feat**: 新功能
- **fix**: Bug 修复
- **docs**: 文档修改
- **style**: 代码风格修改（不影响功能）
- **refactor**: 重构代码
- **perf**: 性能优化
- **test**: 测试相关
- **chore**: 依赖更新、构建配置等
- **ci**: CI/CD 配置

### 示例
```
feat(notification): 添加通知已读功能

- 实现标记单条通知为已读
- 实现全部标记为已读功能
- 优化通知列表性能

Closes #123
```

## 🏆 代码规范

### Kotlin 代码规范
- 遵循 [Kotlin 官方代码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 使用 4 个空格缩进
- 使用有意义的变量名和函数名
- 为公开 API 添加 KDoc 注释

### 命名规范
- 类名：PascalCase（如 `NotificationViewModel`）
- 函数名：camelCase（如 `getNotifications`）
- 常量名：UPPER_SNAKE_CASE（如 `MAX_RETRY_COUNT`）
- 文件名：与主要类名一致

### 注释规范
```kotlin
/**
 * 获取通知列表
 * 
 * @return 通知列表流
 */
fun getNotifications(): Flow<List<Notification>>
```

## ✅ 提交前检查清单

- [ ] 代码编译通过（`./gradlew build`）
- [ ] 没有编译警告（或已解释）
- [ ] 代码遵循项目规范
- [ ] 添加了必要的注释和文档
- [ ] 没有提交敏感信息
- [ ] 修改不会破坏现有功能
- [ ] 提交信息清晰明确

## 🐛 Bug 报告

如果发现 Bug，请：

1. 检查是否已有相同的 Issue
2. 提供详细的错误描述
3. 包含复现步骤
4. 提供运行环境信息
5. 如可能，提供错误日志

## 💡 功能建议

如有功能建议，请：

1. 描述用例和需求
2. 解释为什么这个功能有用
3. 提供可能的实现方案（如有）
4. 讨论潜在的影响

## 📧 问题？

如有任何问题，请：

1. 查看现有的 Issues
2. 查看文档和 README
3. 在 Discussions 中提问
4. 联系维护者

## 🙏 致谢

感谢所有为项目做出贡献的人！

---

**Happy Coding! 🚀**


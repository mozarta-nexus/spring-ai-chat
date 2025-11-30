# Spring AI Chat Frontend

Vue3 + Axios 前端聊天界面，支持流式响应。

## 功能特性

- ✅ 用户输入和消息发送
- ✅ 流式响应显示（实时显示AI回复）
- ✅ 普通响应模式（一次性显示完整回复）
- ✅ 美观的现代化UI设计
- ✅ 自动滚动到最新消息
- ✅ 响应式布局

## 安装依赖

```bash
npm install
```

## 开发运行

```bash
npm run dev
```

应用将在 `http://localhost:3000` 启动。

## 构建生产版本

```bash
npm run build
```

## 配置说明

- 后端API地址：`http://localhost:8080`
- 开发环境已配置代理，直接使用 `/api` 路径即可
- 流式响应使用 Fetch API 的流式读取功能

## 使用说明

1. 在输入框中输入消息
2. 按 `Enter` 发送消息，`Shift+Enter` 换行
3. 可以选择"流式响应"或"普通响应"模式
4. 流式响应模式下，AI回复会实时显示，就像打字一样




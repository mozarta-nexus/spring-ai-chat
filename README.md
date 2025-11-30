# Spring AI Chat

一个基于 Spring AI 和 Vue 3 构建的现代化 AI 聊天应用，支持流式响应和对话记忆功能。

## 📋 项目简介

Spring AI Chat 是一个前后端分离的 AI 聊天应用，后端使用 Spring Boot 3.5.8 和 Spring AI 框架，前端使用 Vue 3 和 Vite。项目集成了阿里云 DashScope（通义千问）API，提供流畅的对话体验。

## ✨ 功能特性

- 🤖 **AI 对话**：基于 Spring AI 框架，支持与 AI 模型进行自然对话
- 💬 **流式响应**：支持实时流式输出，提供更好的用户体验
- 🧠 **对话记忆**：支持多轮对话上下文记忆，保持对话连贯性
- 🎨 **现代化 UI**：美观的渐变设计，响应式布局
- 📝 **Markdown 支持**：支持 Markdown 格式渲染，包括代码高亮
- 🔄 **新建对话**：支持创建新对话，清空上下文
- ⚡ **快速响应**：前后端分离架构，性能优异

## 🛠️ 技术栈

### 后端
- **Spring Boot** 3.5.8
- **Spring AI** 1.1.0
- **Java** 17
- **Maven** 构建工具
- **Lombok** 简化代码

### 前端
- **Vue 3** 组合式 API
- **Vite** 5.0 构建工具
- **Axios** HTTP 客户端
- **Marked** Markdown 解析
- **Highlight.js** 代码高亮

## 📁 项目结构

```
spring-ai-chat/
├── front/                    # 前端项目
│   ├── src/
│   │   ├── components/      # Vue 组件
│   │   │   └── ChatWindow.vue
│   │   ├── App.vue          # 主应用组件
│   │   ├── main.js          # 入口文件
│   │   └── style.css        # 全局样式
│   ├── index.html           # HTML 模板
│   ├── package.json         # 前端依赖
│   └── vite.config.js       # Vite 配置
├── src/
│   └── main/
│       ├── java/
│       │   └── com/mozarta/springaichat/
│       │       ├── config/          # 配置类
│       │       │   ├── ChatConfiguration.java
│       │       │   └── WebMvcConfiguration.java
│       │       ├── controller/      # 控制器
│       │       │   └── ChatController.java
│       │       └── SpringAiChatApplication.java
│       └── resources/
│           └── application.yaml    # 应用配置
├── pom.xml                  # Maven 配置
└── README.md               # 项目说明
```

## 🔧 环境要求

- **JDK** 17 或更高版本
- **Maven** 3.6+ 
- **Node.js** 16+ 和 **npm** 8+

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd spring-ai-chat
```

### 2. 配置后端

编辑 `src/main/resources/application.yaml` 文件，配置你的 API 密钥：

```yaml
spring:
  ai:
    openai:
      api-key: your-api-key-here  # 替换为你的 API 密钥
      chat:
        options:
          model: "qwen3-max"       # 使用的模型
      base-url: "https://dashscope.aliyuncs.com/compatible-mode"
```

### 3. 启动后端

```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run

# 或使用 Maven
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 4. 启动前端

```bash
cd front
npm install
npm run dev
```

前端服务将在 `http://localhost:5173` 启动（Vite 默认端口）。

### 5. 访问应用

在浏览器中打开 `http://localhost:5173` 即可开始使用。

## ⚙️ 配置说明

### 后端配置

在 `application.yaml` 中可以配置以下参数：

- `spring.ai.openai.api-key`: API 密钥（必填）
- `spring.ai.openai.chat.options.model`: 使用的模型名称
- `spring.ai.openai.base-url`: API 基础 URL

### 前端配置

如果需要修改后端 API 地址，编辑 `front/src/components/ChatWindow.vue` 中的 `API_BASE_URL`：

```javascript
const API_BASE_URL = 'http://localhost:8080'
```

## 📡 API 接口

### 普通聊天

```
GET /api/chat?input={message}&conversation_id={id}
```

**参数：**
- `input` (必填): 用户输入的消息
- `conversation_id` (可选): 对话 ID，用于保持上下文

**响应：**
```json
"AI 回复的文本内容"
```

### 流式聊天

```
GET /api/chat/stream?input={message}&conversation_id={id}
```

**参数：**
- `input` (必填): 用户输入的消息
- `conversation_id` (可选): 对话 ID，用于保持上下文

**响应：**
流式文本响应（`text/html;charset=utf8`）

## 🎯 使用说明

1. **发送消息**：在输入框中输入消息，按 `Enter` 发送，`Shift+Enter` 换行
2. **流式响应**：默认启用流式响应模式，可以实时看到 AI 回复
3. **普通响应**：切换到普通响应模式，等待完整回复后一次性显示
4. **新建对话**：点击"新建对话"按钮，清空当前对话上下文
5. **对话记忆**：系统会自动保存对话 ID，刷新页面后可以继续之前的对话

## 🛠️ 开发说明

### 构建前端

```bash
cd front
npm run build
```

构建产物将输出到 `front/dist` 目录。

### 打包后端

```bash
./mvnw clean package
```

打包后的 JAR 文件位于 `target/spring-ai-chat-0.0.1-SNAPSHOT.jar`。

### 运行测试

```bash
./mvnw test
```

## 📝 注意事项

1. **API 密钥**：请妥善保管你的 API 密钥，不要提交到版本控制系统
2. **跨域问题**：如果前后端运行在不同端口，需要配置 CORS（项目已包含 CORS 配置）
3. **对话记忆**：对话 ID 存储在浏览器 localStorage 中，清除浏览器数据会丢失对话上下文
4. **模型选择**：当前配置使用 `qwen3-max` 模型，可根据需要修改

## 🔒 安全建议

- 不要将 API 密钥提交到 Git 仓库
- 生产环境建议使用环境变量或配置中心管理敏感信息
- 考虑添加 API 调用频率限制
- 建议添加用户认证和授权机制

## 📄 许可证

本项目采用 MIT 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题或建议，请通过 Issue 联系。


# AI Helper

一个支持**流式对话、多轮记忆、知识库问答（RAG）、联网搜索（MCP）**的 AI 健康助手。
前后端分离：Spring Boot 3 + LangChain4j 后端，Vue 3 前端。

## 功能特性

- **流式对话**：基于 SSE 的打字机式输出（Reactor `Flux`），首字即时响应
- **多轮对话记忆**：对话上下文持久化到 MySQL（自定义 `ChatMemoryStore`），服务重启不丢失，按用户隔离
- **知识库问答（RAG）**：私有文档按段落切分（1000 字符 / 重叠 200），通义 Embedding 向量化后检索增强生成
- **联网搜索（MCP）**：通过 MCP 协议（HTTP+SSE 传输）接入智谱网页搜索工具，模型可获取实时信息
- **输入安全护轨**：LangChain4j `InputGuardrail` 拦截敏感词请求
- **登录 / 注册**：JWT 无状态鉴权，拦截器统一校验，ThreadLocal 透传用户身份
- **调研报告生成**：输入主题一键生成结构化报告，带工具调用次数与超时限制
- **Token 用量统计**：记录每次模型调用的 token 消耗
- **前端**：Markdown 渲染（marked）+ XSS 过滤（DOMPurify），登录态本地持久化

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 21 / Spring Boot 3.5 / LangChain4j 1.1 / MyBatis-Plus / MySQL / JJWT |
| 前端 | Vue 3 / Vite / Axios / marked / DOMPurify |
| AI | 通义千问（qwen-max / text-embedding-v4）、智谱 MCP 网页搜索 |

## 架构概览

```
Vue 3 SPA ──axios / EventSource──> Spring Boot 3
                                     │
                     LangChain4j AiService（声明式接口 + 流式）
                                     │
        ┌──────────────┬─────────────┼──────────────┬─────────────┐
        ▼              ▼             ▼              ▼             ▼
   通义千问chat    通义Embedding   MCP客户端      MySQL       输入护轨
   (DashScope)    + 内存向量库   (智谱网页搜索)  用户/消息/    (敏感词拦截)
                    (RAG检索)                  记忆/Token统计
```

## 项目结构

```
ai-helper/
├── langchain4j-ai-helper/        后端 Spring Boot 工程
│   └── src/main/java/com/example/aicodehelper/
│       ├── ai/                   LangChain4j 配置、AiService、RAG、MCP、护轨
│       ├── controller/           REST 与 SSE 接口
│       ├── interceptor/          JWT 令牌校验拦截器
│       ├── mapper/ service/      数据访问与业务逻辑
│       └── pojo/                 DTO / Entity / VO
└── ai-helper-frontend/           前端 Vue 3 工程
    └── src/
        ├── api/                  接口封装（含 SSE 事件流处理）
        └── utils/                登录态与通用工具
```

## 快速开始

### 环境要求

- JDK 21、MySQL 8、Node.js 16+
- 通义千问 API Key（DashScope）、智谱 API Key（MCP 网页搜索）

### 后端

1. 初始化数据库表：执行 `langchain4j-ai-helper/src/main/resources/schema.sql`
2. 配置密钥：新建 `langchain4j-ai-helper/src/main/resources/application-local.yml`
   （该文件已被 `.gitignore` 排除，不会进入仓库；也可改用系统环境变量）

   ```yaml
   DASHSCOPE_API_KEY: 你的通义千问Key
   BIGMODEL_API_KEY: 你的智谱Key
   MYSQL_USERNAME: root
   MYSQL_PASSWORD: 你的数据库密码
   JWT_SECRET: 至少32位的随机字符串
   ```

3. 启动：

   ```bash
   cd langchain4j-ai-helper
   mvnw.cmd spring-boot:run      # Linux/Mac 用 ./mvnw
   ```

### 前端

```bash
cd ai-helper-frontend
npm install
npm run dev
```

浏览器打开 Vite 提示的地址（默认 `http://localhost:5173`），注册账号后即可对话。

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/ai/login` `/ai/register` | 登录 / 注册，返回 JWT |
| GET | `/ai/chat` | 流式对话（SSE） |
| GET | `/ai/chatSync` | 同步对话 |
| GET | `/ai/messages` | 拉取当前用户聊天记录 |
| POST | `/ai` | 保存一轮聊天记录 |
| POST | `/ai/report` | 生成调研报告 |

## 说明

- 仓库**不包含任何真实密钥**：所有敏感配置通过环境变量 / `application-local.yml` 注入
- 向量库当前为内存实现（`InMemoryEmbeddingStore`），重启后需重新摄入文档

## Roadmap

- 向量库替换为 Redis 持久化存储
- 调研报告支持导出 Markdown / PDF
- 补充单元测试与接口压测报告

# 饮食健康 AI 助手（AI Health Helper）

基于 **Spring Boot 3 + LangChain4j** 的饮食健康与体重管理智能助手，集成流式对话、RAG 知识库问答、工具调用（Function Calling）、MCP 联网搜索与结构化研究报告生成。

## 项目亮点

- **流式对话（SSE）**：基于 Project Reactor 的 `Flux` + Server-Sent Events，实现打字机式逐字输出，提供同步 / 流式两种模式。
- **RAG 检索增强生成**：内置饮食健康知识库（Markdown 文档），经段落切片 → 向量化（text-embedding-v4）→ 相似度召回，将领域知识注入 Prompt，提升回答的专业度与准确性。
- **工具调用（Function Calling）**：自定义 `calculateBMI` 工具，由模型自主决定何时调用完成 BMI 计算，规避大模型“心算”数值出错。
- **MCP 联网搜索**：通过 Model Context Protocol 接入智谱 `web_search` 工具，让模型获取实时信息。
- **多轮会话记忆**：基于 `memoryId` 为每个用户隔离 `MessageWindowChatMemory`，维护上下文窗口。
- **输入防护栏**：`SafeInputGuardrail` 在调用模型前对用户输入做安全校验。
- **结构化输出**：将研究主题约束为 JSON 结构（`name` + `suggestionList`），便于前端渲染。
- **JWT 鉴权**：注册 / 登录签发令牌，拦截器统一校验，聊天记录按用户持久化到 MySQL。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 基础框架 | Spring Boot 3.5.3 · JDK 21 |
| AI 编排 | LangChain4j 1.1.0 · DashScope（通义千问 qwen-max）|
| 向量模型 | text-embedding-v4 |
| 联网搜索 | LangChain4j MCP · 智谱 web_search |
| 流式处理 | Project Reactor · langchain4j-reactor |
| 持久层 | MyBatis-Plus 3.5.5 · MySQL 8 |
| 鉴权 | JWT（jjwt）|

## 系统架构

```
客户端
  | HTTP / SSE
  v
Controller 层    AuthController(注册/登录)    AiController(对话/报告/记录)
  |
拦截器           JwtTokenInterceptor: 校验 token -> BaseContext 存 userId
  |
AI Service       AiCodeHelperService（@SystemMessage 声明式接口）
  |              由 AiCodeHelperServiceFactory 组装
  |- ChatModel / StreamingChatModel ...... qwen-max 对话
  |- ContentRetriever .................... RAG 检索
  |     \-- InMemoryEmbeddingStore <- docs/*.md 知识库
  |- HealthTools ......................... calculateBMI 工具
  |- McpToolProvider ..................... 智谱 web_search
  \-- SafeInputGuardrail ................. 输入防护
  |
持久层           MyBatis-Plus -> MySQL（用户 / 聊天记录）
```

## 快速开始

### 环境要求

- JDK 21
- MySQL 8.x
- Maven 3.8+（或使用自带的 `mvnw`）

### 配置密钥

项目采用**配置与密钥分离**：提交到仓库的 `application.yml` 只保留 `${占位符}`，真实密钥写入被 `.gitignore` 忽略的 `application-local.yml`。

在 `src/main/resources/` 下新建 `application-local.yml`：

```yaml
DASHSCOPE_API_KEY: 你的阿里云百炼Key
BIGMODEL_API_KEY: 你的智谱开放平台Key
MYSQL_USERNAME: root
MYSQL_PASSWORD: 你的MySQL密码
JWT_SECRET: 你的自定义JWT密钥
```

> 也可通过系统环境变量注入同名变量（如 `export DASHSCOPE_API_KEY=...`），便于服务器部署。

### 初始化数据库

连接串已带 `createDatabaseIfNotExist=true`，首次启动自动建库；表结构见 `src/main/resources/schema.sql`。

### 启动项目

```bash
./mvnw spring-boot:run
```

服务默认运行在 `http://localhost:8080`。

## 接口一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/ai/register` | 用户注册 |
| POST | `/ai/login` | 登录，返回 JWT |
| GET | `/ai/chat?message=` | 流式对话（SSE）|
| GET | `/ai/chatSync?message=` | 同步对话 |
| POST | `/ai/report` | 生成结构化研究报告 |
| GET | `/ai/logs?limit=` | 查询当前用户对话记录 |
| POST | `/ai/logs` | 保存一条对话记录 |

> 除注册 / 登录外，接口需在请求头携带 `token`。

## 目录结构

```
src/main/java/com/example/aicodehelper
|- ai/               AI 能力：config(模型/RAG/MCP) · service · tools · guardrail
|- controller/       接口层
|- service/          业务层（登录、研究报告）
|- mapper/           持久层 Mapper
|- entity/           数据库实体
|- dto/              请求数据对象
|- vo/               响应视图对象
|- intercepetor/     JWT 拦截器
|- properties/       配置属性绑定
|- context/          线程级用户上下文
\- Utils/            工具类（JwtUtil）
src/main/resources
|- docs/             RAG 知识库文档
|- system-prompt.txt 系统提示词
|- schema.sql        建表脚本
\- application.yml   主配置
```

## 优化路线

- [ ] 向量库持久化（Redis / pgvector），避免每次启动重复 ingest
- [ ] 会话记忆持久化，支持服务重启后上下文恢复
- [ ] RAG 引入 rerank 重排序，提升召回质量
- [ ] 研究报告改异步（线程池 / MQ），避免长请求阻塞
- [ ] 基于 Redis + Lua 的用户级限流，防止 LLM API 被刷
- [ ] Docker 化部署与前端演示页

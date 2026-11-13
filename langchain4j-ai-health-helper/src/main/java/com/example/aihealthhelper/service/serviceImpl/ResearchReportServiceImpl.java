package com.example.aihealthhelper.service.serviceImpl;

import com.example.aihealthhelper.service.ResearchReportService;
import com.example.aihealthhelper.pojo.vo.ResearchReportVO;
import com.example.aihealthhelper.pojo.vo.ResearchReportVO.Source;
import com.example.aihealthhelper.pojo.vo.ResearchReportVO.ToolTrace;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResearchReportServiceImpl implements ResearchReportService {

    private static final String REPORT_SYSTEM_PROMPT = """
            你是饮食健康、科学减肥与食物热量分析助手。只基于用户提供的来源（如饮食记录、食品营养成分表、体检数据等）撰写减脂与营养评估报告；引用来源编号，如 [F1]（食品标签）、[D1]（饮食记录）、[S1]（科学文献）。没有来源支撑的热量估算或减脂判断要标注“待验证”。不要编造食物热量、营养数据、文献或链接。输出用 Markdown，结构为：减脂目标界定、关键营养与热量事实、执行路径与关键指标、参考来源。
            """;
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s\"'<>]+");

    // 工具调用次数限制
    @Value("${research.report.max-tool-calls:2}")
    private int maxToolCalls;
    // 每步超时时间（秒）
    @Value("${research.report.step-timeout-seconds:20}")
    private long stepTimeoutSeconds;
    // 生成超时时间（秒）
    @Value("${research.report.generation-timeout-seconds:40}")
    private long generationTimeoutSeconds;

    // 依赖注入 省略Autowired
    // 内容检索器
    private final ContentRetriever contentRetriever;
    // 大模型客户端
    private final McpClient mcpClient;
    // 聊天模型
    private final ChatModel chatModel;
    //Json翻译官
    private final ObjectMapper objectMapper;


    // 构造函数
    public ResearchReportServiceImpl(@Lazy ContentRetriever contentRetriever,
                                     McpClient bigModelMcpClient,
                                     @Qualifier("myQwenChatModel") ChatModel chatModel,
                                     ObjectMapper objectMapper) {
        this.contentRetriever = contentRetriever;
        this.mcpClient = bigModelMcpClient;
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    // 生成研究报告
    @Override
    public ResearchReportVO generate(String topic) {
        // 记录报告生成开始时间
        long start = System.currentTimeMillis();
        // 初始化来源列表和工具调用列表
        List<Source> sources = new ArrayList<>();
        List<ToolTrace> toolTrace = new ArrayList<>();
        // 初始化工具调用计数
        int toolCalls = 0;

        // 本地检索
        String localContext = "";
        // 记录本地检索开始时间
        long stepStart = System.currentTimeMillis();
        try {
            List<Content> contents = timed(() -> contentRetriever.retrieve(Query.from(topic)), stepTimeoutSeconds);
            toolCalls++;
            int index = 1;
            for (Content content : contents) {
                TextSegment segment = content.textSegment();
                Metadata metadata = segment.metadata();
                String title = metadata == null ? null : metadata.getString("file_name");
                if (title == null || title.isBlank()) {
                    title = "本地文档";
                }
                Object score = content.metadata() == null
                        ? null : content.metadata().get(ContentMetadata.SCORE);
                Double scoreValue = score instanceof Number number ? number.doubleValue() : null;
                // 添加来源
                sources.add(new Source("S" + index, "local", title, null,
                            abbreviate(segment.text(), 600), scoreValue));
                index++;
            }
            localContext = buildContext(sources);
            toolTrace.add(new ToolTrace("本地检索", "embedding_store",
                    contents.isEmpty() ? "empty" : "success",
                    "命中 " + contents.size() + " 个文本段", elapsed(stepStart)));
        } catch (TimeoutException e) {
            toolTrace.add(new ToolTrace("本地检索", "embedding_store", "timeout",
                    "超过 " + stepTimeoutSeconds + " 秒，已降级", elapsed(stepStart)));
        } catch (Exception e) {
            toolTrace.add(new ToolTrace("本地检索", "embedding_store", "failed",
                    rootMessage(e), elapsed(stepStart)));
        }

        // 联网检索
        String webContext = "";
        if (toolCalls < maxToolCalls) {
            stepStart = System.currentTimeMillis();
            try {
                String webResult = timed(() -> searchWeb(topic), stepTimeoutSeconds);
                toolCalls++;
                if (webResult == null || webResult.isBlank()) {
                    toolTrace.add(new ToolTrace("联网检索", "mcp:web_search", "empty",
                            "工具返回为空", elapsed(stepStart)));
                } else {
                    sources.add(new Source("W1", "web", "智谱 web_search", extractUrl(webResult),
                            abbreviate(webResult, 800), null));
                    webContext = "[W1] " + abbreviate(webResult, 4000);
                    toolTrace.add(new ToolTrace("联网检索", "mcp:web_search", "success",
                            "返回 " + webResult.length() + " 字符", elapsed(stepStart)));
                }
            } catch (TimeoutException e) {
                toolTrace.add(new ToolTrace("联网检索", "mcp:web_search", "timeout",
                        "超过 " + stepTimeoutSeconds + " 秒，继续使用本地来源", elapsed(stepStart)));
            } catch (Exception e) {
                toolTrace.add(new ToolTrace("联网检索", "mcp:web_search", "failed",
                        rootMessage(e) + "，继续使用本地来源", elapsed(stepStart)));
            }
        } else {
            toolTrace.add(new ToolTrace("联网检索", "mcp:web_search", "skipped",
                    "已达到最大工具调用次数 " + maxToolCalls, 0));
        }

        // 生成答案
        stepStart = System.currentTimeMillis();
        String answer;
        try {
            String finalLocalContext = localContext;
            String finalWebContext = webContext;
            answer = timed(() -> generateAnswer(topic, finalLocalContext, finalWebContext), generationTimeoutSeconds);
            if (answer == null || answer.isBlank()) {
                throw new IllegalStateException("模型返回为空");
            }
            toolTrace.add(new ToolTrace("报告生成", "qwen-max", "success",
                    "生成 " + answer.length() + " 字符", elapsed(stepStart)));
        } catch (TimeoutException e) {
            answer = fallbackReport(topic, sources);
            toolTrace.add(new ToolTrace("报告生成", "qwen-max", "timeout",
                    "超过 " + generationTimeoutSeconds + " 秒，返回检索摘要", elapsed(stepStart)));
        } catch (Exception e) {
            answer = fallbackReport(topic, sources);
            toolTrace.add(new ToolTrace("报告生成", "qwen-max", "failed",
                    rootMessage(e) + "，返回检索摘要", elapsed(stepStart)));
        }

        boolean hasFailure = toolTrace.stream()
                .anyMatch(trace -> trace.status().equals("failed") || trace.status().equals("timeout"));
        ResearchReportVO.Status status = sources.isEmpty() || hasFailure
                ? ResearchReportVO.Status.DEGRADED
                : ResearchReportVO.Status.SUCCESS;
        return new ResearchReportVO(status, topic, answer, sources, toolTrace, elapsed(start));
    }

    private String searchWeb(String topic) throws Exception {
        List<ToolSpecification> tools = mcpClient.listTools();
        if (tools == null || tools.isEmpty()) {
            throw new IllegalStateException("MCP 未返回可用工具");
        }
        ToolSpecification tool = tools.stream()
                .filter(spec -> spec.name() != null && spec.name().toLowerCase().contains("search"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("MCP 未提供搜索工具"));
        String arguments = objectMapper.writeValueAsString(Map.of(chooseArgumentName(tool), topic));
        return mcpClient.executeTool(ToolExecutionRequest.builder()
                .id(UUID.randomUUID().toString())
                .name(tool.name())
                .arguments(arguments)
                .build());
    }

    private String chooseArgumentName(ToolSpecification tool) {
        JsonObjectSchema parameters = tool.parameters();
        if (parameters != null && parameters.properties() != null && !parameters.properties().isEmpty()) {
            Map<String, JsonSchemaElement> properties = parameters.properties();
            for (String candidate : List.of("query", "search_query", "q", "keyword", "question")) {
                if (properties.containsKey(candidate)) {
                    return candidate;
                }
            }
            List<String> required = parameters.required();
            if (required != null && !required.isEmpty() && properties.containsKey(required.get(0))) {
                return required.get(0);
            }
            return properties.keySet().iterator().next();
        }
        return "query";
    }

    private String generateAnswer(String topic, String localContext, String webContext) {
        String prompt = """
                研究主题：%s

                本地知识库来源：
                %s

                联网检索来源：
                %s

                请生成研究入门报告。
                """.formatted(topic, blankToNone(localContext), blankToNone(webContext));
        // 调用模型生成答案 springAI自带的.chat()方法
        return chatModel.chat(SystemMessage.from(REPORT_SYSTEM_PROMPT), UserMessage.from(prompt))
                .aiMessage()
                .text();
    }
    // 回退报告
    private String fallbackReport(String topic, List<Source> sources) {
        StringBuilder report = new StringBuilder("模型生成暂时不可用，先返回检索摘要。\n\n研究主题：")
                .append(topic)
                .append("\n\n");
        if (sources.isEmpty()) {
            report.append("未命中可用来源，建议更换关键词或补充本地文档。");
            return report.toString();
        }
        for (Source source : sources) {
            report.append('[').append(source.id()).append("] ").append(source.title()).append('\n')
                    .append(source.snippet()).append("\n\n");
        }
        return report.toString();
    }

    // 来源
    private String buildContext(List<Source> sources) {
        StringBuilder context = new StringBuilder();
        for (Source source : sources) {
            context.append('[').append(source.id()).append("] ").append(source.title()).append('\n')
                    .append(source.snippet()).append("\n\n");
        }
        return context.toString();
    }

    // 超时控制
    private <T> T timed(Callable<T> task, long timeoutSeconds) throws Exception {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw new RuntimeException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private String extractUrl(String text) {
        Matcher matcher = URL_PATTERN.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String rootMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getSimpleName() : abbreviate(message, 200);
    }

    private String blankToNone(String text) {
        return text == null || text.isBlank() ? "无" : text;
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "…";
    }

    private long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }
}
package com.example.aihealthhelper.ai.service;

import com.example.aihealthhelper.ai.tools.HealthTools;
import com.example.aihealthhelper.service.serviceImpl.MysqlChatMemoryStore;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class AiHealthHelperServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private StreamingChatModel myQwenStreamingChatModel;

    @Resource
    @Lazy
    private ContentRetriever contentRetriever;

    @Resource
    private McpToolProvider mcpToolProvider;


    @Resource
    private MysqlChatMemoryStore chatMemoryStore;


    @Bean
    public AiHealthHelperService aiCodeHelperService() {
        // 手动构造 AI Service（Ai Service的声明式创建）
        AiHealthHelperService aiHealthHelperService = AiServices.builder(AiHealthHelperService.class)
                .chatModel(myQwenChatModel) // 用于同步响应场景
                .streamingChatModel(myQwenStreamingChatModel) // 用于流式响应场景
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .contentRetriever(contentRetriever) // RAG 检索增强生成
                .tools(new HealthTools())
                .toolProvider(mcpToolProvider)
                .build();
        return aiHealthHelperService;
    }
}

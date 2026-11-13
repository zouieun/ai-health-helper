package com.example.aihealthhelper.ai.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.community.dashscope.chat-model")
@Data
public class QwenChatModelConfig {

    private String modelName;

    private String apiKey;

    @Resource
    private ChatModelListener chatModelListener;

    @Value("${langchain4j.community.dashscope.streaming-chat-model.model-name}") // 不在prefix下面，独立导入
    private String streamingModelName;

    @Value("${langchain4j.community.dashscope.streaming-chat-model.api-key}")  // 不在prefix下面，独立导入
    private String streamingApiKey;

    @Bean
    public ChatModel myQwenChatModel() {
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .listeners(List.of(chatModelListener)) //自定义模型的监听器，监听聊天模型（ChatModel）的请求、响应和错误事件
                .build();
    }

    @Bean
    public StreamingChatModel myQwenStreamingChatModel() {
        return QwenStreamingChatModel.builder()
                .apiKey(streamingApiKey)
                .modelName(streamingModelName)
                .listeners(List.of(chatModelListener))
                .build();
    }
}

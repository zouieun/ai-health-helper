package com.example.aihealthhelper.ai.config;

import com.example.aihealthhelper.mapper.TokenUsageMapper;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ChatModelListenerConfig {

    @Bean
    ChatModelListener chatModelListener(TokenUsageMapper tokenUsageMapper) {
        return new ChatModelListener() {
            @Override
            public void onResponse(ChatModelResponseContext responseContext) {
                TokenUsage usage = responseContext.chatResponse().tokenUsage();
                log.info("AI response: tokenUsage={}", usage);
                if (usage != null) {
                    try {
                        tokenUsageMapper.record(usage.inputTokenCount(),
                                usage.outputTokenCount(), usage.totalTokenCount());
                    } catch (Exception e) {
                        log.warn("token 用量记录失败", e);
                    }
                }
            }

            @Override
            public void onError(ChatModelErrorContext errorContext) {
                log.error("AI call failed", errorContext.error());
            }
        };
    }
}
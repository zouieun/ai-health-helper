package com.example.aicodehelper.ai.service;

import com.example.aicodehelper.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.*;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

/**@AiService 自动创建AI Service（Ai Service的声明式创建）
AiCodeHelperServiceFactory 创建AI Service的工厂类*/
@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeHelperService {

    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String userMessage);


    // 流式对话
    @SystemMessage(fromResource = "system-prompt.txt")// 系统提示
    Flux<String> chatStream(@MemoryId long memoryId, @UserMessage String userMessage);
}

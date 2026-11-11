package com.example.aicodehelper.ai.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    //智谱API Key
    @Value("${bigmodel.api-key}")
    private String apiKey;

    @Bean
    public McpClient bigModelMcpClient() {
        // 创建 HTTP+SSE 传输层：URL携带API Key完成鉴权
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("https://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + apiKey)
                .logRequests(true) // 开启日志，查看更多信息
                .logResponses(true)
                .build();
        // 基于传输层创建默认 MCP 客户端，Key为客户端唯一标识
        return new DefaultMcpClient.Builder()
                .key("aiCodeHelperMcpClient")
                .transport(transport)
                .build();
    }

    // 将MCP客户端暴露的工具包装成McpToolProvider
    @Bean
    public McpToolProvider mcpToolProvider(McpClient bigModelMcpClient) {
        return McpToolProvider.builder()
                .mcpClients(bigModelMcpClient)
                .build();
    }
}

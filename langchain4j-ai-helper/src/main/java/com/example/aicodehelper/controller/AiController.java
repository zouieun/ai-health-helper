package com.example.aicodehelper.controller;

import com.example.aicodehelper.ai.service.AiCodeHelperService;
import com.example.aicodehelper.common.Result;
import com.example.aicodehelper.pojo.dto.ResearchReportDTO;
import com.example.aicodehelper.pojo.vo.ResearchReportVO;
import com.example.aicodehelper.service.MessageService;
import com.example.aicodehelper.service.ResearchReportService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import com.example.aicodehelper.pojo.entity.Message;

import static com.example.aicodehelper.context.BaseContext.getCurrentId;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Resource
    private MessageService messageService;

    @Resource
    private ResearchReportService researchReportService;

    //Flux<> 当作传送带，源源不断地传送数据
    //SSE<> 包装盒，用于包装数据，使其符合SSE协议
    //chatStream 方法调用大模型流式API，返回Flux<String>
    //map 方法将 Flux<String> 转换为 Flux<ServerSentEvent<String>>
    //Flux<String> ->> string == chunk ->> ServerSentEvent<String> ->> Flux<ServerSentEvent<String>>
    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(String message) {
        return aiCodeHelperService.chatStream(getCurrentId(), message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }//chat//流式输出不用Result

    @GetMapping("/chatSync")
    public Result<String> chatSync(@RequestParam("message") String message) {
        return Result.success(aiCodeHelperService.chat(message));
    }//chatSync

    //record 极简定义只读数据类 编译器会自动生成全参构造函数、访问器方法(如 topic() )、toString、equals、hashCode，专当纯数据容器
    @PostMapping("/report")
    public Result<ResearchReportVO> report(@RequestBody ResearchReportDTO requestDTO) {
        // 校验请求参数
        if (requestDTO == null || requestDTO.topic() == null || requestDTO.topic().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "topic 不能为空");
        }
        String topic = requestDTO.topic().trim();
        if (topic.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "topic 长度不能超过 200 字符");
        }
        // 调用生成报告服务
        return Result.success(researchReportService.generate(topic));
    }

    // 获取用户聊天记录
    @GetMapping("/messages")
    public Result<List<Message>> listMessages(@RequestParam(value = "limit", defaultValue = "20") int limit) {
        return Result.success(messageService.listMessages(getCurrentId(), limit));
    }//messages

    // 保存用户聊天记录
    @PostMapping
    public Result<Void> saveMessage(@RequestBody Message message) {
        messageService.saveMessage(getCurrentId(), message);
        return Result.success();
    }//saveMessage
}

package com.example.aicodehelper.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_memory")
public class ChatMemory {

    @TableId(type = IdType.INPUT)
    private Long memoryId;

    private String messagesJson;

    private LocalDateTime updatedAt;
}
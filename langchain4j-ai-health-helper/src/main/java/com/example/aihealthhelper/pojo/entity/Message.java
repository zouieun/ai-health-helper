package com.example.aihealthhelper.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String requestContent;

    private String responseContent;

    private String errorContent;

    private LocalDateTime createdAt;

}

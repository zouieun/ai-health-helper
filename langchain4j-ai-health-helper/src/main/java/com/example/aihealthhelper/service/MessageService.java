package com.example.aihealthhelper.service;

import com.example.aihealthhelper.pojo.entity.Message;

import java.util.List;

public interface MessageService {
    //根据用户ID获取消息列表
    List<Message> listMessages(Long userId, int limit);
    //保存消息
    void saveMessage(Long userId, Message message);
}

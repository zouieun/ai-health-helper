package com.example.aicodehelper.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.aicodehelper.pojo.entity.Message;
import com.example.aicodehelper.mapper.MessageMapper;
import com.example.aicodehelper.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public List<Message> listMessages(Long userId, int limit) {
        int pageSize = Math.max(1,Math.min(limit, 100));
        return messageMapper.selectList(new QueryWrapper<Message>()
                .eq("user_id", userId)
                .orderByDesc("id")
                .last("limit " + pageSize));
    }
    @Override
    public void saveMessage(Long userId, Message message) {
        message.setId(null);
        message.setUserId(userId);
        messageMapper.insert(message);
    }
}

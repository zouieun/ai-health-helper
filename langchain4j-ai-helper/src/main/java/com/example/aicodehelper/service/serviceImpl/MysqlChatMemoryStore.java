package com.example.aicodehelper.service.serviceImpl;

import com.example.aicodehelper.pojo.entity.ChatMemory;
import com.example.aicodehelper.mapper.ChatMemoryMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MysqlChatMemoryStore implements ChatMemoryStore {

    @Resource
    private ChatMemoryMapper chatMemoryMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        ChatMemory row = chatMemoryMapper.selectById((Long) memoryId);
        return row == null ? List.of() : ChatMessageDeserializer.messagesFromJson(row.getMessagesJson());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        ChatMemory row = new ChatMemory();
        row.setMemoryId((Long) memoryId);
        row.setMessagesJson(ChatMessageSerializer.messagesToJson(messages));
        if (chatMemoryMapper.selectById((Long) memoryId) != null) {
            chatMemoryMapper.updateById(row);
        } else {
            chatMemoryMapper.insert(row);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMemoryMapper.deleteById((Long) memoryId);
    }
}
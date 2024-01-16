package com.aichatapi.controller;

import com.aichatapi.controller.input.ChatMessage;
import com.aichatapi.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ChatController {

    private final AIChatService aiChatService;

    public ChatController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }


    @MessageMapping("/startChat")
    public void startTask(ChatMessage chatMessage) {
        // fixme chatSessionId should be sent on header
        log.info("start chat session {}", chatMessage.chatSessionId());
        this.aiChatService.chat(chatMessage);
    }
}

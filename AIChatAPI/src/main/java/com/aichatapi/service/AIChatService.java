package com.aichatapi.service;

import com.aichatapi.controller.input.ChatMessage;
import com.aichatapi.event.ChatEvent;
import com.aichatapi.event.ChatEventData;
import com.aichatapi.exception.AIChatException;
import com.aichatapi.service.openai.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AIChatService {

    private final ChatService openAIService;
    private final ChatService googleGeminiService;
    private final ApplicationEventPublisher publisher;

    public AIChatService(OpenAIService openAIService,
                         GoogleGeminiService googleGeminiService,
                         ApplicationEventPublisher publisher) {
        this.openAIService = openAIService;
        this.googleGeminiService = googleGeminiService;
        this.publisher = publisher;
    }

    public void chat(ChatMessage chatMessage) {
        var chatSessionId = chatMessage.chatSessionId();
        var openAIStoppedTheConversion = false;
        var googleGeminiStoppedTheConversation = false;

        var chatGPTMessage = this.chatWithOpenAI(chatMessage.message(), chatSessionId);
        var googleGeminiMessage = this.chatWithGoogleGemini(chatGPTMessage, chatSessionId);
        while (!openAIStoppedTheConversion && !googleGeminiStoppedTheConversation) {
            chatGPTMessage = this.chatWithOpenAI(googleGeminiMessage, chatSessionId);
            openAIStoppedTheConversion = this.openAIService.stopChat(chatGPTMessage);

            googleGeminiMessage = this.chatWithGoogleGemini(chatGPTMessage, chatSessionId);
            googleGeminiStoppedTheConversation = this.googleGeminiService.stopChat(googleGeminiMessage);
        }

        log.info("Chat Session {} ended", chatSessionId);
        this.publishMessage(new ChatEventData("conversation stopped", "/all/stop-" + chatSessionId));

    }

    private String chatWithOpenAI(String message, Integer chatSessionId) {
        try {
            var chatGPTMessage = this.openAIService.chat(message, chatSessionId);
            Thread.sleep(4000);
            this.publishMessage(new ChatEventData(chatGPTMessage, "/all/openAIMessages-" + chatSessionId));
            return chatGPTMessage;

        } catch (InterruptedException e) {
            throw new AIChatException(STR. "an error occurred when processing open ai chat \{ e.getMessage() }" );
        }
    }

    private String chatWithGoogleGemini(String message, Integer chatSessionId) {
        try {
            var googleGeminiMessage = this.googleGeminiService.chat(message, chatSessionId);
            Thread.sleep(4000);
            this.publishMessage(new ChatEventData(googleGeminiMessage, "/all/googleGeminiMessages-" + chatSessionId));
            return googleGeminiMessage;
        } catch (InterruptedException e) {
            throw new AIChatException(STR. "an error occurred when processing google gemini chat \{ e.getMessage() }" );
        }
    }


    private void publishMessage(ChatEventData chatEventData) {
        this.publisher.publishEvent(new ChatEvent(this, chatEventData));
    }
}

package com.aichatapi.service;

import com.aichatapi.config.GoogleGeminiConfiguration;
import com.aichatapi.exception.AIChatException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GoogleGeminiService {

    private final GoogleGeminiConfiguration googleGeminiConfiguration;
    private final Cache<String, ChatSession> googleGeminiChatSessionsCash =
            Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .maximumSize(100)
                    .build();

    public GoogleGeminiService(GoogleGeminiConfiguration googleGeminiConfiguration) {
        this.googleGeminiConfiguration = googleGeminiConfiguration;
    }

    public void initGeminiChatSession(Integer chatSessionId) throws IOException {
        var vertexAI = new VertexAI(this.googleGeminiConfiguration.getProjectId(), this.googleGeminiConfiguration.getLocation());
        var model = new GenerativeModel(this.googleGeminiConfiguration.getModelName(), vertexAI);
        var chatSession = new ChatSession(model);
        var contentUser = Content.newBuilder()
                .setRole("user")
                .addParts(Part.newBuilder().setText(this.googleGeminiConfiguration.getGoogleGeminiContext().replace("\n", "")).build())
                .build();

        var contentModel = Content.newBuilder()
                .setRole("model")
                .addParts(Part.newBuilder().setText("""
                        Understood.
                        """).build())
                .build();
        var contents = new ArrayList<Content>();
        contents.add(contentUser);
        contents.add(contentModel);
        chatSession.setHistory(contents);
        googleGeminiChatSessionsCash.put(chatSessionId.toString(), chatSession);

    }

    public String chat(String message, Integer chatSessionId) {
        try {
            var chatSession = this.getChatSession(chatSessionId);
            var response = ResponseHandler.getText(chatSession.sendMessage(message));

            log.info("Google gemini content {} for chatSessionId {}", response, chatSession);
            return response;
        } catch (IOException e) {
            throw new AIChatException(STR. "an error occurred when sending message to google gemini \{ e.getMessage() }" );
        }
    }


    private ChatSession getChatSession(Integer chatSessionId) {
        return Optional.ofNullable(this.googleGeminiChatSessionsCash
                        .getIfPresent(chatSessionId.toString()))
                .orElseGet(() -> {
                    try {
                        this.initGeminiChatSession(chatSessionId);
                        return this.getChatSession(chatSessionId);
                    } catch (IOException e) {
                        throw new AIChatException(STR. "an occurred when sending message to google gemini \{ e.getMessage() }" );
                    }
                });
    }


}

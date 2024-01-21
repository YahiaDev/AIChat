package com.aichatapi.service;

import com.aichatapi.config.GoogleGeminiConfiguration;
import com.aichatapi.exception.AIChatException;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
public class GoogleGeminiService implements ChatService {

    private final GoogleGeminiConfiguration googleGeminiConfiguration;

    private final CacheManager caffeineCacheManager;

    public GoogleGeminiService(GoogleGeminiConfiguration googleGeminiConfiguration, CacheManager caffeineCacheManager) {
        this.googleGeminiConfiguration = googleGeminiConfiguration;
        this.caffeineCacheManager = caffeineCacheManager;
    }

    public ChatSession buildChatSession() throws IOException {
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
        return chatSession;
    }

    @Override
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
        Cache cache = this.getCache("google_gemini_request_cache");
        return Optional.ofNullable(cache
                        .get(chatSessionId, ChatSession.class))
                .orElseGet(() -> {
                    try {
                        cache.put(chatSessionId, this.buildChatSession());
                        return cache.get(chatSessionId, ChatSession.class);
                    } catch (IOException e) {
                        throw new AIChatException(STR. "an occurred when sending message to google gemini \{ e.getMessage() }" );
                    }
                });
    }

    private Cache getCache(String cacheName) {
        return Optional.ofNullable(this.caffeineCacheManager.getCache(cacheName))
                .orElseThrow(() -> new AIChatException(STR. "cache name  \{ cacheName } not configured" ));
    }

    @Override
    public boolean stopChat(String message) {
        return ObjectUtils.isEmpty(message)
                || message.contains(this.googleGeminiConfiguration.getStopChatWord());
    }


}

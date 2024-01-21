package com.aichatapi.service.openai;

import com.aichatapi.config.OpenAIConfiguration;
import com.aichatapi.exception.AIChatException;
import com.aichatapi.service.ChatService;
import com.aichatapi.service.openai.model.Message;
import com.aichatapi.service.openai.model.OpenAiChatRequest;
import com.aichatapi.service.openai.model.OpenAiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OpenAIService implements ChatService {

    private final CacheManager caffeineCacheManager;

    private final OpenAIConfiguration openAIConfiguration;


    public OpenAIService(CacheManager CaffeineCacheManager, OpenAIConfiguration openAIConfiguration) {
        this.caffeineCacheManager = CaffeineCacheManager;
        this.openAIConfiguration = openAIConfiguration;
    }


    @Override
    public String chat(String message, Integer chatSessionId) {
        try {
            var request = this.getOpenAIChatRequest(chatSessionId);
            request.messages().add(new Message("user", message));
            var response = this.openAIConfiguration.getRestTemplate().postForObject(this.openAIConfiguration.getCompletionUrl(), request, OpenAiChatResponse.class);

            if (ObjectUtils.isNotEmpty(response)
                    && ObjectUtils.isNotEmpty(response.choices())
                    && ObjectUtils.isNotEmpty(response.choices().get(0).message())
                    && ObjectUtils.isNotEmpty(response.choices().get(0).message().content())) {
                var openAiMessage = response.choices().get(0).message().content();
                request.messages().add(new Message("system", openAiMessage));
                log.info("openAi content {} for chatSessionId {}", response, chatSessionId);
                return openAiMessage;
            }

            if (this.retry(chatSessionId)) {
                return this.chat(message, chatSessionId);
            }

            throw new AIChatException("Cannot get message from open ai");
        } catch (Exception exception) {
            log.error("chat error occurred when getting content from open AI {} with chatSessionId {}", exception.getMessage(), chatSessionId);
            if (this.retry(chatSessionId)) {
                return this.chat(message, chatSessionId);
            }

            throw new AIChatException(STR. "Cannot get message from open ai \{ exception.getMessage() }" );
        }
    }

    private boolean retry(Integer chatSessionId) {
        Cache cache = this.getCache("open_ai_retry_request_cache");
        Integer numberOfRetry = cache.get(chatSessionId, Integer.class);

        if (ObjectUtils.isEmpty(numberOfRetry)) {
            cache.put(chatSessionId, 1);
            return true;
        }

        if (ObjectUtils.isNotEmpty(numberOfRetry) && numberOfRetry <= this.openAIConfiguration.getMaxRetryCall()) {
            cache.put(chatSessionId, ++numberOfRetry);
            return true;
        }

        return false;
    }

    private Cache getCache(String cacheName) {
        return Optional.ofNullable(this.caffeineCacheManager.getCache(cacheName))
                .orElseThrow(() -> new AIChatException(STR. "cache name  \{ cacheName } not configured" ));
    }

    private OpenAiChatRequest getOpenAIChatRequest(Integer chatSessionId) {
        var cache = this.getCache("open_ai_request_cache");
        return Optional.ofNullable(cache.get(chatSessionId, OpenAiChatRequest.class))
                .orElseGet(() -> {
                            cache.put(chatSessionId,
                                    new OpenAiChatRequest(this.openAIConfiguration.getModel(),
                                            new ArrayList<>(List.of(new Message("system", this.openAIConfiguration.getOpenAIContext()))),
                                            this.openAIConfiguration.getN(),
                                            this.openAIConfiguration.getTemperature()));
                            return cache.get(chatSessionId, OpenAiChatRequest.class);
                        }
                );
    }


    @Override
    public boolean stopChat(String message) {
        return ObjectUtils.isEmpty(message) || message.contains(this.openAIConfiguration.getStopChatWord());
    }

}

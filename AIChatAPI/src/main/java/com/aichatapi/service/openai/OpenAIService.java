package com.aichatapi.service.openai;

import com.aichatapi.config.OpenAIConfiguration;
import com.aichatapi.exception.AIChatException;
import com.aichatapi.service.openai.model.Message;
import com.aichatapi.service.openai.model.OpenAiChatRequest;
import com.aichatapi.service.openai.model.OpenAiChatResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OpenAIService {

    private final OpenAIConfiguration openAIConfiguration;
    private final Cache<Integer, OpenAiChatRequest> openAIChatRequestCash =
            Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .maximumSize(100)
                    .build();
    private final Cache<Integer, Integer> openAICallRetry =
            Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .maximumSize(100)
                    .build();

    public OpenAIService(OpenAIConfiguration openAIConfiguration) {
        this.openAIConfiguration = openAIConfiguration;
    }

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
        Integer numberOfRetry = this.openAICallRetry.getIfPresent(chatSessionId);

        if (ObjectUtils.isEmpty(numberOfRetry)) {
            this.openAICallRetry.put(chatSessionId, 1);
            return true;
        }

        if (ObjectUtils.isNotEmpty(numberOfRetry) && numberOfRetry <= this.openAIConfiguration.getMaxRetryCall()) {
            this.openAICallRetry.put(chatSessionId, ++numberOfRetry);
            return true;
        }

        return false;
    }

    private OpenAiChatRequest getOpenAIChatRequest(Integer chatSessionId) {
        return Optional
                .ofNullable(this.openAIChatRequestCash.getIfPresent(chatSessionId))
                .orElseGet(() -> {
                    this.openAIChatRequestCash.put(chatSessionId,
                            new OpenAiChatRequest(this.openAIConfiguration.getModel(),
                                    new ArrayList<>(List.of(new Message("system", this.openAIConfiguration.getOpenAIContext()))),
                                    this.openAIConfiguration.getN(),
                                    this.openAIConfiguration.getTemperature()));
                    return this.openAIChatRequestCash.getIfPresent(chatSessionId);
                });
    }


   /* @Cacheable(value = "jsonCache", key = "#key")
    public JSONObject getOpenAiParamsCash(String key) {
        // Simulating some time-consuming process to generate the JSON object
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", key);
        return jsonObject;
    }*/

    @CacheEvict(value = "jsonCache", key = "#key")
    public void evictCachedJsonObject(String key) {
        // This method can be used to evict or clear the cache for a specific key
    }


}

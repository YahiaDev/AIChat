package com.aichatapi.service.openai.model;

import java.util.List;

public record OpenAiChatResponse(List<Choice> choices) {
}

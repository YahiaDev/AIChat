package com.aichatapi.service.openai.model;

import lombok.Getter;

import java.util.List;


public record OpenAiChatRequest(String model, List<Message> messages,int n ,double temperature) {
}

package com.aichatapi.service;

public interface ChatService {

    String chat(String message, Integer chatSessionId);

    boolean stopChat(String message);
}

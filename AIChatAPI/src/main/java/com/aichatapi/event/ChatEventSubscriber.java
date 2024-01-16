package com.aichatapi.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatEventSubscriber implements ApplicationListener<ChatEvent> {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatEventSubscriber(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void onApplicationEvent(ChatEvent event) {
        // fixme use convertAndSendToUser
        simpMessagingTemplate
                .convertAndSend(event.getChatEventData().destination(), event.getChatEventData().message());
    }
}

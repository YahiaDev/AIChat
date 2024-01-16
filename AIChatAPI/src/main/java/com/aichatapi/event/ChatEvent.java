package com.aichatapi.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChatEvent extends ApplicationEvent {


    private final ChatEventData chatEventData;

    public ChatEvent(Object source, ChatEventData chatEventData) {
        super(source);
        this.chatEventData = chatEventData;
    }






}


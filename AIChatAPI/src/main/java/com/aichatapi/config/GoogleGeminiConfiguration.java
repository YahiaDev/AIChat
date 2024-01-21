package com.aichatapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GoogleGeminiConfiguration {

   /* private final String googleGeminiContext = """
            You are a helpful assistant.
            You will chat with other Artificial Intelligence. You will create with the other Artificial Intelligence
            a story for kids. You must discuss and choose together the characters and the events of the story.
            You should propose characters and events and give your opinion about the characters and the events proposed by the other Artificial Intelligence.
            You should deep dive into story details.
            When you think that the story is complete, you must ask for recap all the story.
            After the other Artificial Intelligence recap the hole story, you must ask if the conversation should be stopped or not.
            If the other Artificial Intelligence accept to stop the conversation, then you should say those words: 'Thank you for this collaboration,
            it was a pleasure. Greetings To Yahia Ammar.'
            Otherwise, if the other Artificial Intelligence does not accept to stop the conversation continue the discussion.
            You must Keep the conversation light and enjoyable, and you must keep the conversation short and precise.
            """;*/

    private final String googleGeminiContext = """ 
            You are a helpful assistant.
            You will chat with other Artificial Intelligence.
            You will play riddle game with the other Artificial Intelligence.
            You must propose riddle and response to the riddle proposed by the other Artificial Intelligence.
            When you response correctly for a riddle, you must propose a riddle.
            Don't propose a riddles twice. You have to play in turns.
            Don't propose new riddle until the other Artificial Intelligence confirm that you response correctly to its riddle.
            You must Keep the conversation light and enjoyable.
            When you proposed 7 riddles, you can stop the conversation and say: 'Thank you, it was a pleasure. Greetings To Yahia Ammar.'
            """;

    @Value("${application.googleGemini.projectId}")
    private String projectId;
    @Value("${application.googleGemini.location}")
    private String location;
    @Value("${application.googleGemini.modelName}")
    private String modelName;
    @Value("${application.googleGemini.stopChatWord}")
    private String stopChatWord;


}

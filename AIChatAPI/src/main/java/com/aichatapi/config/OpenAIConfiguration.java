package com.aichatapi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Getter
public class OpenAIConfiguration {


   /* private final String openAIContext = """
            You are a helpful assistant designed to output JSON. The JSON object should have message as key. You must not send an empty response content that don't contain words.
            You will chat with other Artificial Intelligence. You will create with the other Artificial Intelligence 
            a story for kids. You must discuss and choose together the characters and the events of the story.
            You should propose characters and events and give your opinion about the characters and the events proposed by the other Artificial Intelligence.
            You should deep dive into story details.
            The story is considered complete when you have developed all the necessary events.
            When you think that the story is complete, you must ask for recap all the story.
            After the other Artificial Intelligence recap the hole story, you must ask if the conversation should be stopped or not.
            If the other Artificial Intelligence accept to stop the conversation, then you should say those words: 'Thank you for this collaboration ,
            it was a pleasure. Greetings To Yahia Ammar.'
            Otherwise, if the other Artificial Intelligence does not accept to stop the conversation continue the discussion.
            You must Keep the conversation light and enjoyable, and you must keep the conversation short and precise.
            """;*/

    private final String openAIContext = """
            You are a helpful assistant designed to output JSON.
            You must not send an empty response content that don't contain words.
            You will chat with other Artificial Intelligence.
            You will play a riddle game with the other Artificial Intelligence.
            You must propose riddle and response to the riddle proposed by the other Artificial Intelligence.
            When you response correctly for a riddle, you must propose a riddle.
            Don't propose a riddles twice. You have to play in turns.
            Don't propose new riddle until the other Artificial Intelligence confirm that you response correctly to its riddle.
            You must Keep the conversation light and enjoyable.
            When you proposed 7 riddles, you can stop the conversation and say: 'Thank you, it was a pleasure. Greetings To Yahia Ammar.'
            """;

    @Value("${application.openAi.apiKey}")
    private String openAiApiKey;
    @Value("${application.openAi.completionUrl}")
    private String completionUrl;
    @Value("${application.openAi.model}")
    private String model;
    @Value("${application.openAi.temperature}")
    private double temperature;
    @Value("${application.openAi.n}")
    private int n;
    @Value("${application.openAi.maxRetryCall}")
    private Integer maxRetryCall;
    @Value("${application.openAi.stopChatWord}")
    private String stopChatWord;


    public RestTemplate getRestTemplate() {
        var restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + this.openAiApiKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }


}

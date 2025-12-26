package com.hurindigital.springgrokbot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OpenAIChatService implements ChatService {

    private final ChatClient chatClient;

    public OpenAIChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public ResponseSpec ask(String query, Object conversationId) {
        return new ResponseSpec() {
            @Override
            public Flux<String> immediate() {
                return chatClient.prompt()
                        .user(query)
                        .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .stream()
                        .content();
            }

            @Override
            public Mono<String> complete() {
                return immediate()
                        .reduce("", String::concat);
            }

        };
    }

}

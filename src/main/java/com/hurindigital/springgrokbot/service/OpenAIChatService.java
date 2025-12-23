package com.hurindigital.springgrokbot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import reactor.core.publisher.Mono;

public class OpenAIChatService implements ChatService {

    private final ChatClient chatClient;

    public OpenAIChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Mono<String> ask(String query) {
        return ask(query, ChatMemory.DEFAULT_CONVERSATION_ID);
    }

    @Override
    public Mono<String> ask(String query, Object conversationId) {
        return Mono.justOrEmpty(chatClient.prompt()
                .user(query)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content());
    }

}

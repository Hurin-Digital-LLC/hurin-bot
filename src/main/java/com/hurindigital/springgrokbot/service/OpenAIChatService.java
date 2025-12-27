package com.hurindigital.springgrokbot.service;

import com.hurindigital.springgrokbot.discord.Chunker;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class OpenAIChatService implements ChatService {

    private final ChatClient chatClient;

    private final ChatMemory chatMemory;

    public OpenAIChatService(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
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
            public Flux<String> complete() {
                return immediate()
                        .reduce("", String::concat)
                        .as(Chunker::chunk);
            }
        };
    }

    @Override
    public void appendChat(String conversationId, String message) {
        log.info("Appending message '{}' to conversation {}", message, conversationId);
        chatMemory.add(conversationId, UserMessage.builder()
                        .text(message)
                .build());
    }

}

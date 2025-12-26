package com.hurindigital.springgrokbot.service;

import org.springframework.ai.chat.memory.ChatMemory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {

    default ResponseSpec ask(String query) {
        return ask(query, ChatMemory.DEFAULT_CONVERSATION_ID);
    }

    ResponseSpec ask(String query, Object conversationId);

    interface ResponseSpec {

        Flux<String> immediate();

        Mono<String> complete();

    }

}

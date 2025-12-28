package com.hurindigital.springgrokbot.service;

import jakarta.annotation.Nullable;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.content.Media;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface ChatService {

    default ResponseSpec ask(String query) {
        return ask(query, ChatMemory.DEFAULT_CONVERSATION_ID);
    }

    default ResponseSpec ask(String query, Object conversationId) {
        return ask(query, conversationId, null);
    }

    ResponseSpec ask(String query, Object conversationId, @Nullable Collection<Media> media);

    void appendChat(String conversationId, String message);

    interface ResponseSpec {

        /**
         * @return a Flux immediately regardless if data is still being published.
         */
        Flux<String> immediate();

        /**
         * @return a complete response in appropriately sized chunks.
         */
        Flux<String> complete();

    }

}

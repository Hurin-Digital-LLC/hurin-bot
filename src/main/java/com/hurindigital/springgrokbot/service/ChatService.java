package com.hurindigital.springgrokbot.service;

import reactor.core.publisher.Mono;

public interface ChatService {

    Mono<String> ask(String query);

    Mono<String> ask(String query, Object conversationId);

}

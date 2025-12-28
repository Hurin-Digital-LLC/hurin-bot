package com.hurindigital.springgrokbot.service;

import com.hurindigital.springgrokbot.domain.Thread;
import com.hurindigital.springgrokbot.domain.ThreadEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ThreadTrackerService {

    Mono<? extends Thread> track(long threadId);

    Mono<? extends Thread> close(Thread thread);

    Mono<ThreadEntity> find(long id);

    Mono<Boolean> exists(long id);

    Flux<? extends Thread> findAllActiveThreads();

}

package com.hurindigital.springgrokbot.service;

import com.hurindigital.springgrokbot.domain.Thread;
import com.hurindigital.springgrokbot.domain.ThreadEntity;
import com.hurindigital.springgrokbot.repo.ThreadRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class DiscordThreadTrackerService implements ThreadTrackerService {

    private final ThreadRepository threadRepository;

    public DiscordThreadTrackerService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    @Override
    public Mono<? extends Thread> track(long threadId) {
        return threadRepository.findByThreadId(threadId)
                .switchIfEmpty(Mono.defer(() -> threadRepository.save(ThreadEntity.builder()
                                .threadId(threadId)
                                .created(Instant.now())
                        .build())));
    }

    @Override
    public Mono<Void> close(Thread thread) {
        return threadRepository.delete(ThreadEntity.from(thread));
    }

    @Override
    public Mono<ThreadEntity> find(long id) {
        return threadRepository.findByThreadId(id);
    }

    @Override
    public Mono<Boolean> exists(long id) {
        return threadRepository.existsByThreadId(id);
    }

    @Override
    public Flux<? extends Thread> findAllActiveThreads() {
        return threadRepository.findAll();
    }

}

package com.hurindigital.springgrokbot.service;

import com.hurindigital.springgrokbot.domain.ThreadEntity;
import com.hurindigital.springgrokbot.repo.ThreadRepository;
import reactor.core.publisher.Mono;

public class DiscordThreadTrackerService implements ThreadTrackerService {

    private final ThreadRepository threadRepository;

    public DiscordThreadTrackerService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    @Override
    public Mono<ThreadEntity> track(ThreadEntity thread) {
        return threadRepository.findByThreadId(thread.getThreadId())
                .switchIfEmpty(Mono.defer(() -> threadRepository.save(thread)));
    }

    @Override
    public Mono<ThreadEntity> find(long id) {
        return threadRepository.findByThreadId(id);
    }
}

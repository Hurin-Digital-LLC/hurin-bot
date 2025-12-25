package com.hurindigital.springgrokbot.service;

import com.hurindigital.springgrokbot.domain.Thread;
import com.hurindigital.springgrokbot.repo.ThreadRepository;
import reactor.core.publisher.Mono;

public class DiscordThreadTrackerService implements ThreadTrackerService {

    private final ThreadRepository threadRepository;

    public DiscordThreadTrackerService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    @Override
    public Mono<Thread> track(Thread thread) {
        return threadRepository.findByThreadId(thread.getThreadId())
                .switchIfEmpty(Mono.defer(() -> threadRepository.save(thread)));
    }

    @Override
    public Mono<Thread> find(long id) {
        return threadRepository.findByThreadId(id);
    }
}

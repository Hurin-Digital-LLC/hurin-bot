package com.hurindigital.springgrokbot.service;

import com.hurindigital.springgrokbot.domain.ThreadEntity;
import reactor.core.publisher.Mono;

public interface ThreadTrackerService {

    Mono<ThreadEntity> track(ThreadEntity thread);

    Mono<ThreadEntity> find(long id);

    Mono<Boolean> exists(long id);

}

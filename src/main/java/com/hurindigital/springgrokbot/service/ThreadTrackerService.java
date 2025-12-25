package com.hurindigital.springgrokbot.service;

import com.hurindigital.springgrokbot.domain.Thread;
import reactor.core.publisher.Mono;

public interface ThreadTrackerService {

    Mono<Thread> track(Thread thread);

    Mono<Thread> find(long id);

}

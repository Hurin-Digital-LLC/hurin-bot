package com.hurindigital.springgrokbot.repo;

import com.hurindigital.springgrokbot.domain.Thread;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ThreadRepository extends R2dbcRepository<Thread, Long> {

    Mono<Thread> findByThreadId(long threadId);

}

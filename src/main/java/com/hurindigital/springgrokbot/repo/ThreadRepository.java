package com.hurindigital.springgrokbot.repo;

import com.hurindigital.springgrokbot.domain.ThreadEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
public interface ThreadRepository extends R2dbcRepository<ThreadEntity, Long> {

    Mono<ThreadEntity> findByThreadId(long threadId);

    Mono<Boolean> existsByThreadId(long threadId);

    Flux<ThreadEntity> findAllByClosedIsNull();

}

package com.hurindigital.springgrokbot.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@Table("thread")
public class ThreadEntity implements Thread {

    @Id
    private final UUID id;

    private final long threadId;

    private final Instant created;

    private final Instant closed;

    public static ThreadEntity from(Thread thread) {
        return builder()
                .id(thread.getId())
                .threadId(thread.getThreadId())
                .created(thread.getCreated())
                .closed(thread.getClosed())
                .build();
    }

}

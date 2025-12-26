package com.hurindigital.springgrokbot.domain;

import discord4j.core.object.entity.channel.ThreadChannel;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("thread")
public class ThreadEntity implements Thread {

    @Id
    private final UUID id;

    private final long threadId;

    public static ThreadEntity from(ThreadChannel channel) {
        return builder()
                .threadId(channel.getId().asLong())
                .build();
    }

}

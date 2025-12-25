package com.hurindigital.springgrokbot.domain;

import discord4j.core.object.entity.channel.ThreadChannel;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class Thread {

    private final UUID id;

    private final long threadId;

    public static Thread from(ThreadChannel channel) {
        return builder()
                .id(UUID.randomUUID())
                .threadId(channel.getId().asLong())
                .build();
    }

}

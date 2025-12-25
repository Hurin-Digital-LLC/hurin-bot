package com.hurindigital.springgrokbot.discord.function.ask;

import com.hurindigital.springgrokbot.discord.EventHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class AskReplyHandler implements EventHandler<MessageCreateEvent> {

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return null;
    }

}

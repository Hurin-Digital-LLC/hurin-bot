package com.hurindigital.springgrokbot.discord.function.ask;

import com.hurindigital.springgrokbot.discord.EventHandler;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class AskReplyHandler implements EventHandler<MessageCreateEvent> {

    private final ThreadTrackerService threadTrackerService;

    public AskReplyHandler(ThreadTrackerService threadTrackerService) {
        this.threadTrackerService = threadTrackerService;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return Mono.empty();
    }

}

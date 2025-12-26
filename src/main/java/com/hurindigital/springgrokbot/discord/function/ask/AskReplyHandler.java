package com.hurindigital.springgrokbot.discord.function.ask;

import com.hurindigital.springgrokbot.discord.EventHandler;
import com.hurindigital.springgrokbot.domain.ThreadEntity;
import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public class AskReplyHandler implements EventHandler<MessageCreateEvent> {

    private final ThreadTrackerService threadTrackerService;

    private final ChatService chatService;

    public AskReplyHandler(ThreadTrackerService threadTrackerService, ChatService chatService) {
        this.threadTrackerService = threadTrackerService;
        this.chatService = chatService;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getAuthor())
                .filter(user -> !user.isBot())
                .flatMap(_ -> event.getMessage().getChannel())
                .ofType(ThreadChannel.class)
                .filterWhen(thread -> threadTrackerService.exists(thread.getId().asLong()))
                .flatMap(thread -> Mono.justOrEmpty(event.getMessage().getContent())
                        .filter(content -> !content.isBlank())
                        .switchIfEmpty(Mono.fromRunnable(() -> log.debug("Empty message in thread {}", thread.getId().asString())))
                        .flatMap(query -> thread.type()
                                .then(chatService.ask(query, thread.getId().asString())
                                        .complete())
                                .flatMap(response -> thread.createMessage(response)
                                        .then()))
                        .onErrorResume(throwable -> {
                            log.error(throwable.getMessage(), throwable);
                            return Mono.empty();
                        }));
    }

}

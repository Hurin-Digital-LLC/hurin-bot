package com.hurindigital.springgrokbot.discord.function.ask;

import com.hurindigital.springgrokbot.discord.EventHandler;
import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.ThreadChannel;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public class AskReplyHandler implements EventHandler<MessageCreateEvent> {

    private static final String TRIGGER = "!hurin";

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
                .filter(isBot().negate())
                .flatMap(toThreadChannel(event))
                .filterWhen(isTrackedThread())
                .flatMap(thread -> Mono.justOrEmpty(event.getMessage().getContent())
                        .filter(content -> !content.isBlank())
                        .switchIfEmpty(Mono.fromRunnable(() -> log.debug("Empty message in thread {}", thread.getId().asString())))
                        .flatMap(query -> {
                            if (isTriggerQuery(query)) {
                                return thread.type()
                                        .then(chatService.ask(clean(query), thread.getId().asString())
                                                .complete()
                                                .delayElements(Duration.ofMillis(600))
                                                .flatMap(thread::createMessage, 1)
                                                .then());
                            } else {
                                chatService.appendChat(getConversationId(thread), query);
                                return Mono.empty();
                            }
                        })
                        .onErrorResume(throwable -> {
                            log.error(throwable.getMessage(), throwable);
                            return Mono.empty();
                        }));
    }

    private String getConversationId(ThreadChannel thread) {
        return thread.getId().asString();
    }

    private Predicate<User> isBot() {
        return User::isBot;
    }

    private Function<User, Mono<ThreadChannel>> toThreadChannel(MessageCreateEvent event) {
        return _ -> event.getMessage().getChannel()
                .ofType(ThreadChannel.class);
    }

    private Function<ThreadChannel, Mono<Boolean>> isTrackedThread() {
        return thread -> threadTrackerService.exists(thread.getId().asLong());
    }

    private boolean isTriggerQuery(String content) {
        return content.trim().toLowerCase().startsWith(TRIGGER.toLowerCase());
    }

    private String clean(String content) {
        return content.trim().substring(TRIGGER.length()).trim();
    }

}

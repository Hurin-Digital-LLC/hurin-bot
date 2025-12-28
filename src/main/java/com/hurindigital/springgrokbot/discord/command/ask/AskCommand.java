package com.hurindigital.springgrokbot.discord.command.ask;

import com.hurindigital.springgrokbot.discord.command.ChatInteractionCommand;
import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.StartThreadFromMessageSpec;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
public class AskCommand implements ChatInteractionCommand {

    public static final String QUERY_OPTION = "query";

    private final ChatService chatService;

    private final ThreadTrackerService threadTrackerService;

    public AskCommand(ChatService chatService, ThreadTrackerService threadTrackerService) {
        this.chatService = chatService;
        this.threadTrackerService = threadTrackerService;
    }

    @Override
    public String getName() {
        return "ask";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final String query = getQuery(event);
        return event.deferReply()
                .then(event.createFollowup("Let's continue this conversation in the thread below."))
                .flatMap(startThread(query))
                .flatMap(sendResponse(query))
                .thenReturn(event)
                .onErrorResume(sendErrorReply(event))
                .then();
    }

    private Function<Message, Mono<ThreadChannel>> startThread(String name) {
        return message -> message.startThread(StartThreadFromMessageSpec.builder()
                        .name(name)
                .build())
                .flatMap(trackThread());
    }

    private Function<ThreadChannel, Mono<ThreadChannel>> trackThread() {
        return thread -> threadTrackerService.track(thread.getId().asLong())
                .thenReturn(thread);
    }

    private Function<ThreadChannel, Mono<ThreadChannel>> sendResponse(String query) {
        return thread -> thread.type()
                .then(chatService.ask(query, thread.getId().asString())
                        .complete()
                        .delayElements(Duration.ofMillis(600))
                        .flatMap(thread::createMessage, 1)
                        .then(Mono.just(thread))
                );
    }

    private Function<Throwable, Mono<ChatInputInteractionEvent>> sendErrorReply(ChatInputInteractionEvent event) {
        return error -> {
            log.error("Error while asking for a thread", error);
            return event.createFollowup("Failed to create thread: " + error.getMessage())
                    .withEphemeral(true)
                    .thenReturn(event);
        };
    }

    private String getQuery(ChatInputInteractionEvent event) {
        return event.getOption(QUERY_OPTION)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                //TODO: Better error handling
                .orElseThrow();
    }

}

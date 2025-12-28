package com.hurindigital.springgrokbot.discord;

import com.hurindigital.springgrokbot.discord.command.ChatInteractionCommand;
import com.hurindigital.springgrokbot.discord.command.MessageInteractionCommand;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
public class CommandDispatcher implements EventHandler<ApplicationCommandInteractionEvent> {

    private final Set<ChatInteractionCommand> chatInteractionCommands;

    private final Set<MessageInteractionCommand> messageInteractionCommands;

    public CommandDispatcher(Set<ChatInteractionCommand> chatInteractionCommands, Set<MessageInteractionCommand> messageInteractionCommands) {
        this.chatInteractionCommands = chatInteractionCommands;
        this.messageInteractionCommands = messageInteractionCommands;
    }

    @Override
    public Class<ApplicationCommandInteractionEvent> getEventType() {
        return ApplicationCommandInteractionEvent.class;
    }

    @Override
    public Mono<Void> handle(ApplicationCommandInteractionEvent event) {
        if (event instanceof MessageInteractionEvent messageInteractionEvent) {
            return dispatch(messageInteractionEvent);
        }

        if (event instanceof ChatInputInteractionEvent chatInputInteractionEvent) {
            return dispatch(chatInputInteractionEvent);
        }

        return Mono.empty();
    }

    private Mono<Void> dispatch(ChatInputInteractionEvent event) {
        return Flux.fromIterable(chatInteractionCommands)
                .filter(cmd -> cmd.getName().equals(event.getCommandName()))
                .next()
                .flatMap(cmd -> cmd.handle(event))
                .then();
    }

    private Mono<Void> dispatch(MessageInteractionEvent event) {
        return Flux.fromIterable(messageInteractionCommands)
                .filter(cmd -> cmd.getName().equals(event.getCommandName()))
                .next()
                .flatMap(cmd -> cmd.handle(event))
                .then();
    }

}

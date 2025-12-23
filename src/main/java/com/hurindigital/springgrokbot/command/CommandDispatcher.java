package com.hurindigital.springgrokbot.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class CommandDispatcher {

    private final Collection<Command> commands;

    public CommandDispatcher(Collection<Command> commands, GatewayDiscordClient discordClient) {
        this.commands = commands;
        discordClient.on(ChatInputInteractionEvent.class, this::handle)
                .subscribe();
    }

    private Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .flatMap(command -> command.handle(event));
    }

}

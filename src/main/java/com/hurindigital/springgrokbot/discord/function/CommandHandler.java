package com.hurindigital.springgrokbot.discord.function;

import com.hurindigital.springgrokbot.discord.EventHandler;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class CommandHandler implements EventHandler<ChatInputInteractionEvent> {

    private final Collection<Command> commands;

    public CommandHandler(Collection<Command> commands) {
        this.commands = commands;
    }

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .flatMap(command -> command.handle(event));
    }

}

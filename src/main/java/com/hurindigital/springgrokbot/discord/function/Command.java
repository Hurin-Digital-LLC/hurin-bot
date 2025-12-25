package com.hurindigital.springgrokbot.discord.function;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface Command {

    String getName();

    Mono<Void> handle(ChatInputInteractionEvent event);

}

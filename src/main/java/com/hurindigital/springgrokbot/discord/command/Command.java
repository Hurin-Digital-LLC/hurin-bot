package com.hurindigital.springgrokbot.discord.command;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import reactor.core.publisher.Mono;

interface Command<T extends ApplicationCommandInteractionEvent> {

    String getName();

    Class<? extends T> getType();

    Mono<Void> handle(T event);

}

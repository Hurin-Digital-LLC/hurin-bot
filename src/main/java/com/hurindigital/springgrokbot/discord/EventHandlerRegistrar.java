package com.hurindigital.springgrokbot.discord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
public class EventHandlerRegistrar implements ApplicationRunner {

    private final GatewayDiscordClient gatewayDiscordClient;

    private final Set<EventHandler<? extends Event>> eventHandlers;

    public EventHandlerRegistrar(GatewayDiscordClient gatewayDiscordClient, Set<EventHandler<? extends Event>> eventHandlers) {
        this.gatewayDiscordClient = gatewayDiscordClient;
        this.eventHandlers = eventHandlers;
    }

    @Override
    public void run(ApplicationArguments args) {
        Mono<Void> combined = Mono.empty();

        for (EventHandler<? extends Event> handler : this.eventHandlers) {
            combined = combined.and(register(handler));
        }

        combined.subscribe(_ -> log.info("All event handlers registered."),
                error -> log.error("An error occurred while registering handlers.", error));
    }

    private <E extends Event> Mono<Void> register(EventHandler<E> handler) {
        Class<E> type = handler.getEventType();
        return gatewayDiscordClient.on(type)
                .flatMap(handler::handle)
                .onErrorResume(error -> {
                    log.error(error.getMessage());
                    return Mono.empty();
                })
                .then();
    }

}

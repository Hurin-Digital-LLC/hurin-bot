package com.hurindigital.springgrokbot.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hurindigital.springgrokbot.config.DiscordConfig;
import com.hurindigital.springgrokbot.config.DiscordProperties;
import discord4j.common.JacksonResources;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class BotRunner implements ApplicationRunner {

    private final DiscordProperties properties;

    private final DiscordClient discordClient;

    private final Set<EventHandler<? extends Event>> eventHandlers;

    private final ObjectMapper mapper = JacksonResources.create().getObjectMapper();

    public BotRunner(DiscordProperties properties, DiscordClient discordClient, Set<EventHandler<? extends Event>> eventHandlers) {
        this.properties = properties;
        this.discordClient = discordClient;
        this.eventHandlers = eventHandlers;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        discordClient.gateway()
                .setEnabledIntents(IntentSet.nonPrivileged().or(IntentSet.of(Intent.MESSAGE_CONTENT)))
                .setEventDispatcher(EventDispatcher.builder().build())
                .login()
                .flatMap(registerEventHandlers())
                .flatMap(registerCommands())
                .flatMap(GatewayDiscordClient::onDisconnect)
                .doOnError(error -> log.error("Discord service error", error))
                .subscribe();
    }

    private Function<GatewayDiscordClient, Mono<GatewayDiscordClient>> registerEventHandlers() {
        return gateway -> Flux.fromIterable(eventHandlers)
                .flatMap(eventHandler -> register(gateway, eventHandler))
                .then(Mono.just(gateway));
    }

    private <E extends Event> Mono<Void> register(GatewayDiscordClient gateway, EventHandler<E> handler) {
        Class<E> type = handler.getEventType();
        return gateway.on(type)
                .flatMap(handler::handle)
                .onErrorResume(error -> {
                    log.error(error.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    private Function<GatewayDiscordClient, Mono<GatewayDiscordClient>> registerCommands() {
        return gateway -> {
            final ApplicationService service = gateway.getRestClient().getApplicationService();
            final Long applicationId = properties.getApplicationId();
            final Long guildId = properties.getGuildId();
            return loadCommands()
                    .collectList()
                    .flatMapMany(commands -> service.bulkOverwriteGuildApplicationCommand(applicationId, guildId, commands))
                    .then(Mono.just(gateway));
        };
    }

    private Flux<ApplicationCommandRequest> loadCommands() {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        return Mono.fromCallable(() -> resolver.getResources("commands/*.json"))
                .flatMapMany(Flux::fromArray)
                .flatMap(resource -> Mono.fromCallable(resource::getInputStream)
                        .flatMap(this::loadResource)
                        .onErrorResume(throwable -> {
                            log.error("Failed to load commands from resource", throwable);
                            return Mono.empty();
                        }))
                .doOnComplete(() -> log.info("Commands loaded successfully"));
    }

    private Mono<ApplicationCommandRequest> loadResource(InputStream inputStream) {
        return Mono.fromCallable(() -> mapper.readValue(inputStream, ApplicationCommandRequest.class));
    }

}

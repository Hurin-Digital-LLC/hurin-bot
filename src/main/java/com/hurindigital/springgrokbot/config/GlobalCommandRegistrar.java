package com.hurindigital.springgrokbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@Slf4j
public class GlobalCommandRegistrar implements ApplicationRunner {

    private final RestClient restClient;

    private final DiscordProperties discordProperties;

    private final ObjectMapper mapper;

    public GlobalCommandRegistrar(RestClient restClient, DiscordProperties discordProperties) {
        this.restClient = restClient;
        this.discordProperties = discordProperties;
        this.mapper = JacksonResources.create().getObjectMapper();
    }

    @Override
    public void run(ApplicationArguments args) {
        final ApplicationService applicationService = restClient.getApplicationService();
        final Long applicationId = discordProperties.getApplicationId();
        final Long guildId = discordProperties.getGuildId();
        loadCommands()
                .collectList()
                .flatMapMany(commands -> applicationService.bulkOverwriteGuildApplicationCommand(applicationId, guildId, commands))
                .subscribe();
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

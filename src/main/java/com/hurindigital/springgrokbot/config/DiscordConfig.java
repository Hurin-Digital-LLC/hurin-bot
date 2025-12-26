package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.discord.EventHandler;
import com.hurindigital.springgrokbot.discord.EventHandlerRegistrar;
import com.hurindigital.springgrokbot.discord.function.Command;
import com.hurindigital.springgrokbot.discord.function.CommandHandler;
import com.hurindigital.springgrokbot.discord.GlobalCommandRegistrar;
import com.hurindigital.springgrokbot.discord.function.ask.AskReplyHandler;
import com.hurindigital.springgrokbot.repo.ThreadRepository;
import com.hurindigital.springgrokbot.service.DiscordThreadTrackerService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.rest.RestClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Set;

@Configuration
public class DiscordConfig {

    @Bean
    @ConfigurationProperties(prefix = "discord")
    DiscordProperties discordProperties() {
        return new DiscordProperties();
    }

    @Bean
    DiscordClient discordClient(DiscordProperties discordProperties) {
        return DiscordClient.create(discordProperties.getToken());
    }

    @Bean
    EventDispatcher discordEventDispatcher() {
        return EventDispatcher.builder().build();
    }

    @Bean
    GatewayDiscordClient gatewayDiscordClient(DiscordClient discordClient, EventDispatcher discordEventDispatcher) {
        return discordClient.gateway()
                .setEventDispatcher(discordEventDispatcher)
                .login()
                .block();
    }

    @Bean
    RestClient discordRestClient(GatewayDiscordClient gatewayDiscordClient) {
        return gatewayDiscordClient.getRestClient();
    }

    @Bean
    GlobalCommandRegistrar commandRegistrar(RestClient discordRestClient, DiscordProperties discordProperties) {
        return new GlobalCommandRegistrar(discordRestClient, discordProperties);
    }

    @Bean
    CommandHandler commandDispatcher(Collection<Command> commands) {
        return new CommandHandler(commands);
    }

    @Bean
    EventHandlerRegistrar eventHandlerRegistrar(GatewayDiscordClient gatewayDiscordClient, Set<EventHandler<? extends Event>> handlers) {
        return new EventHandlerRegistrar(gatewayDiscordClient, handlers);
    }

    @Bean
    ThreadTrackerService threadTrackerService(ThreadRepository threadRepository) {
        return new DiscordThreadTrackerService(threadRepository);
    }

    @Bean
    AskReplyHandler askReplyHandler(ThreadTrackerService threadTrackerService) {
        return new AskReplyHandler(threadTrackerService);
    }

}

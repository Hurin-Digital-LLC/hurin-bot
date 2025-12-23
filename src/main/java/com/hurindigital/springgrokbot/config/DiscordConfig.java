package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.command.Command;
import com.hurindigital.springgrokbot.command.CommandDispatcher;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.rest.RestClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

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
    CommandDispatcher commandDispatcher(Collection<Command> commands, GatewayDiscordClient gatewayDiscordClient) {
        return new CommandDispatcher(commands, gatewayDiscordClient);
    }

}

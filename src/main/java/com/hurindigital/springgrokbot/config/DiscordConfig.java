package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.discord.EventHandler;
import com.hurindigital.springgrokbot.discord.ThreadAutoCloser;
import com.hurindigital.springgrokbot.discord.function.Command;
import com.hurindigital.springgrokbot.discord.function.CommandHandler;
import com.hurindigital.springgrokbot.discord.function.ask.AskCommand;
import com.hurindigital.springgrokbot.discord.function.ask.AskReplyHandler;
import com.hurindigital.springgrokbot.repo.ThreadRepository;
import com.hurindigital.springgrokbot.discord.BotRunner;
import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.DiscordThreadTrackerService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.rest.RestClient;
import jakarta.annotation.PreDestroy;
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
    BotRunner botRunner(DiscordProperties discordProperties, DiscordClient discordClient, Set<EventHandler<? extends Event>> handlers) {
        return new BotRunner(discordProperties, discordClient, handlers);
    }

    @Bean(destroyMethod = "shutdown")
    ThreadAutoCloser threadAutoCloser(DiscordProperties properties, DiscordClient discordClient, ThreadTrackerService threadTrackerService) {
        return new ThreadAutoCloser(properties, discordClient, threadTrackerService);
    }

    @Bean
    AskReplyHandler askReplyHandler(ThreadTrackerService threadTrackerService, ChatService chatService) {
        return new AskReplyHandler(threadTrackerService, chatService);
    }

    @Bean
    CommandHandler commandDispatcher(Collection<Command> commands) {
        return new CommandHandler(commands);
    }

    @Bean
    ThreadTrackerService threadTrackerService(ThreadRepository threadRepository) {
        return new DiscordThreadTrackerService(threadRepository);
    }

    @Bean
    AskCommand askCommand(ChatService chatService, ThreadTrackerService threadTrackerService) {
        return new AskCommand(chatService, threadTrackerService);
    }

    @PreDestroy
    public void shutdown() {

    }

}

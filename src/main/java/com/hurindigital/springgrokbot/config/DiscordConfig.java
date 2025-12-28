package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.discord.*;
import com.hurindigital.springgrokbot.discord.command.ChatInteractionCommand;
import com.hurindigital.springgrokbot.discord.command.MessageInteractionCommand;
import com.hurindigital.springgrokbot.discord.command.ask.AskCommand;
import com.hurindigital.springgrokbot.discord.command.ask.AskHurinCommand;
import com.hurindigital.springgrokbot.repo.ThreadRepository;
import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.DiscordThreadTrackerService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    TrackedThreadReplyHandler askReplyHandler(ThreadTrackerService threadTrackerService, ChatService chatService) {
        return new TrackedThreadReplyHandler(threadTrackerService, chatService);
    }

    @Bean
    CommandDispatcher commandDispatcher(Set<ChatInteractionCommand> chatInteractionCommands, Set<MessageInteractionCommand> messageInteractionCommands) {
        return new CommandDispatcher(chatInteractionCommands, messageInteractionCommands);
    }

    @Bean
    ThreadTrackerService threadTrackerService(ThreadRepository threadRepository) {
        return new DiscordThreadTrackerService(threadRepository);
    }

    @Bean
    AskCommand askCommand(ChatService chatService, ThreadTrackerService threadTrackerService) {
        return new AskCommand(chatService, threadTrackerService);
    }

//    @Bean
//    AskHurinCommand askHurinCommand(ThreadTrackerService threadTrackerService, ChatService chatService) {
//        return new AskHurinCommand(threadTrackerService, chatService);
//    }

}

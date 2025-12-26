package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.discord.function.ask.AskCommand;
import com.hurindigital.springgrokbot.discord.function.HelloCommand;
import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig {

    private final ChatService chatService;

    @Autowired
    public CommandConfig(ChatService chatService) {
        this.chatService = chatService;
    }

    @Bean
    HelloCommand helloCommand() {
        return new HelloCommand();
    }

    @Bean
    AskCommand askCommand(ThreadTrackerService threadTrackerService) {
        return new AskCommand(chatService, threadTrackerService);
    }

}

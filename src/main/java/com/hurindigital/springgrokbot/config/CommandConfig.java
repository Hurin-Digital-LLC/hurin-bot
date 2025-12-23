package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.command.openai.AskCommand;
import com.hurindigital.springgrokbot.command.openai.HelloCommand;
import com.hurindigital.springgrokbot.service.ChatService;
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
    HelloCommand testCommand() {
        return new HelloCommand();
    }

    @Bean
    AskCommand askCommand() {
        return new AskCommand(chatService);
    }

}

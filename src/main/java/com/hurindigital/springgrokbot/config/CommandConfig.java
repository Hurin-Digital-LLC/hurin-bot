package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.command.openai.AskCommand;
import com.hurindigital.springgrokbot.command.openai.HelloCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig {

    @Bean
    HelloCommand testCommand() {
        return new HelloCommand();
    }

    @Bean
    AskCommand askCommand() {
        return new AskCommand();
    }

}

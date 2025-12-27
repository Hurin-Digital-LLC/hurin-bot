package com.hurindigital.springgrokbot.config;

import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.OpenAIChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    ChatService chatService(ChatClient chatClient, ChatMemory chatMemory) {
        return new OpenAIChatService(chatClient, chatMemory);
    }

}

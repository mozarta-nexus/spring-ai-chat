package com.mozarta.springaichat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfiguration {

    @Bean
    ChatClient chatClient(OpenAiChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

}

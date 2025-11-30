package com.mozarta.springaichat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    @GetMapping("/chat")
    public String chat(String input) {
        return chatClient.prompt(input).call().content();
    }

    @GetMapping(value = "/chat/stream", produces = "text/html;charset=utf8")
    public Flux<String> stream(String input, @RequestParam(value = "conversation_id", required = false) String conversationId) {
        if (conversationId == null) {
            return chatClient.prompt(input).stream().content();
        }
        return chatClient.prompt().user(input).advisors(u -> u.param(ChatMemory.CONVERSATION_ID, conversationId)).stream().content();
    }
}

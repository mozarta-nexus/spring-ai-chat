package com.mozarta.springaichat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;

    private final VectorStore vectorStore;

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


    @GetMapping(value = "/rag/chat/stream", produces = "text/html;charset=utf8")
    public Flux<String> rag(String input) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder().topK(3).query(input).build());
        String textTemplate = """
                上下文信息：
                {context}
                
                请根据以上内容，简要专业地回答用户的问题。如果无法从中获取答案，请回复“抱歉，我无法回答这个问题”，不要编造信息。
                
                用户问题：
                {input}
                """;
        PromptTemplate promptTemplate = new PromptTemplate(textTemplate);
        Prompt prompt = promptTemplate.create(Map.of("context", documents.stream().map(Document::getText).collect(Collectors.joining("\n")), "input", input));
        System.out.println(prompt.getContents());
        return chatClient.prompt(prompt).user(input).stream().content();
    }

}

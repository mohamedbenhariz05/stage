package org.example.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.ChatRequest;
import org.example.dto.ChatResponse;
import org.example.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> ask(@Valid @RequestBody ChatRequest request, Principal principal) {
        return ResponseEntity.ok(chatService.ask(principal.getName(), request.getConversationId(), request.getMessage()));
    }
}

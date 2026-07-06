package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final int MAX_HISTORY_MESSAGES = 20;

    private final ObjectMapper objectMapper;
    private final Map<String, Deque<ChatMessage>> conversations = new ConcurrentHashMap<>();

    @Value("${openrouter.base-url:https://openrouter.ai/api/v1}")
    private String openRouterBaseUrl;

    @Value("${openrouter.model:openai/gpt-oss-120b:free}")
    private String openRouterModel;

    @Value("${openrouter.api-key:}")
    private String openRouterApiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public ChatResponse ask(String username, String conversationId, String message) {
        String resolvedConversationId = resolveConversationId(conversationId);
        String conversationKey = buildConversationKey(username, resolvedConversationId);
        Deque<ChatMessage> history = conversations.computeIfAbsent(conversationKey, ignored -> new ArrayDeque<>());
        List<OpenRouterMessage> messages;

        synchronized (history) {
            history.addLast(new ChatMessage("user", message));
            trimHistory(history);
            messages = buildMessages(history);
        }

        OpenRouterChatRequest openRouterRequest = new OpenRouterChatRequest(openRouterModel, messages, false);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(openRouterBaseUrl.replaceAll("/$", "") + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + requireOpenRouterApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(openRouterRequest)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IllegalStateException("OpenRouter returned HTTP " + response.statusCode() + ": " + formatOpenRouterError(response.body()));
            }

            JsonNode body = objectMapper.readTree(response.body());
            String answer = body.path("choices").path(0).path("message").path("content").asText();
            if (answer.isBlank()) {
                throw new IllegalStateException("OpenRouter returned an empty assistant response");
            }

            synchronized (history) {
                history.addLast(new ChatMessage("assistant", answer));
                trimHistory(history);
            }

            return new ChatResponse(resolvedConversationId, answer);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not prepare OpenRouter request", exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not connect to OpenRouter", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenRouter request was interrupted", exception);
        }
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return conversationId.trim();
    }

    private String buildConversationKey(String username, String conversationId) {
        return username + ":" + conversationId;
    }

    private String requireOpenRouterApiKey() {
        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            throw new IllegalStateException("OpenRouter API key is not configured");
        }

        return openRouterApiKey.trim();
    }

    private String formatOpenRouterError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty response body";
        }

        try {
            JsonNode body = objectMapper.readTree(responseBody);
            JsonNode errorNode = body.path("error");
            String error = errorNode.isObject()
                    ? errorNode.path("message").asText()
                    : errorNode.asText();
            if (!error.isBlank()) {
                return error;
            }
        } catch (JsonProcessingException ignored) {
        }

        return responseBody.length() > 300 ? responseBody.substring(0, 300) + "..." : responseBody;
    }

    private List<OpenRouterMessage> buildMessages(Deque<ChatMessage> history) {
        List<OpenRouterMessage> messages = new ArrayList<>();
        messages.add(new OpenRouterMessage("system", """
                You are a helpful assistant. Use the current conversation history as memory.
                If the user asks what they told you earlier, answer from the conversation history.
                Do not say you cannot remember earlier messages when the answer is present in the history.
                """));
        for (ChatMessage chatMessage : history) {
            messages.add(new OpenRouterMessage(chatMessage.role(), chatMessage.content()));
        }

        return messages;
    }

    private void trimHistory(Deque<ChatMessage> history) {
        while (history.size() > MAX_HISTORY_MESSAGES) {
            history.removeFirst();
        }
    }

    private record OpenRouterChatRequest(String model, List<OpenRouterMessage> messages, boolean stream) {
    }

    private record OpenRouterMessage(String role, String content) {
    }

    private record ChatMessage(String role, String content) {
    }
}

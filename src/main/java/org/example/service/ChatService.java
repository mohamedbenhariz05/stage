package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.dto.ChatResponse;
import org.example.model.Cycle;
import org.example.model.Formateur;
import org.example.model.Participant;
import org.example.repo.CycleRepo;
import org.example.repo.FormateurRepo;
import org.example.repo.ParticipantRepo;
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
    private final CycleRepo cycleRepo;
    private final FormateurRepo formateurRepo;
    private final ParticipantRepo participantRepo;

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

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vous êtes l'assistant virtuel intelligent de l'application de gestion des formations et cycles.\n");
        sb.append("Votre rôle est de répondre aux questions des utilisateurs de manière claire, polie et concise en vous basant sur les données actuelles de l'application ci-dessous et l'historique de la conversation.\n\n");

        sb.append("--- DONNÉES ACTUELLES DE L'APPLICATION ---\n\n");

        sb.append("1. CYCLES DE FORMATION :\n");
        List<Cycle> cycles = cycleRepo.findAll();
        if (cycles.isEmpty()) {
            sb.append("  (Aucun cycle de formation enregistré pour le moment)\n");
        } else {
            for (Cycle c : cycles) {
                sb.append(String.format("  - ID: %d | N° Action: %s | Thème: %s | Dates: du %s au %s | Salle: %d | Formateur principal: %s",
                        c.getId(),
                        c.getNumAct(),
                        c.getTheme(),
                        c.getDateDeb(),
                        c.getDateFin(),
                        c.getNumSalle(),
                        c.getForm1()));
                if (c.getForm2() != null && !c.getForm2().isBlank()) {
                    sb.append(" | Formateur 2: ").append(c.getForm2());
                }
                if (c.getForm3() != null && !c.getForm3().isBlank()) {
                    sb.append(" | Formateur 3: ").append(c.getForm3());
                }
                sb.append("\n");
            }
        }
        sb.append("\n");

        sb.append("2. FORMATEURS :\n");
        List<Formateur> formateurs = formateurRepo.findAll();
        if (formateurs.isEmpty()) {
            sb.append("  (Aucun formateur enregistré pour le moment)\n");
        } else {
            for (Formateur f : formateurs) {
                sb.append(String.format("  - ID: %d | Nom & Prénom: %s | Spécialité: %s | Direction: %s | Entreprise: %s\n",
                        f.getId(),
                        f.getNomPrenom(),
                        f.getSpecialite(),
                        f.getDirection(),
                        f.getEntreprise()));
            }
        }
        sb.append("\n");

        sb.append("3. PARTICIPANTS :\n");
        List<Participant> participants = participantRepo.findAll();
        if (participants.isEmpty()) {
            sb.append("  (Aucun participant enregistré pour le moment)\n");
        } else {
            for (Participant p : participants) {
                sb.append(String.format("  - ID: %d | Nom & Prénom: %s | CIN: %s | Entreprise: %s | Thème: %s | Salle: %d | Date Début: %s | Email: %s\n",
                        p.getId(),
                        p.getNomPrenom(),
                        p.getCin(),
                        p.getEntreprise(),
                        p.getTheme(),
                        p.getNumSalle(),
                        p.getDateDebut(),
                        p.getMail() != null ? p.getMail() : "Non renseigné"));
            }
        }
        sb.append("\n--- FIN DES DONNÉES ---");

        return sb.toString();
    }

    private List<OpenRouterMessage> buildMessages(Deque<ChatMessage> history) {
        List<OpenRouterMessage> messages = new ArrayList<>();
        messages.add(new OpenRouterMessage("system", buildSystemPrompt()));
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

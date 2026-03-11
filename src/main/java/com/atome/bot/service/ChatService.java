package com.atome.bot.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ChatService {
    private final ConfigService config;
    private final KnowledgeBaseService kb;
    private final IntentRouterService intent;
    private final CardApplicationAndTrxService cardApplicationAndTrxService;
    private final OverridesService overrides;

    // minimal in-memory state: sessionId -> awaitingTxId
    private final Map<String, Boolean> awaitingTxId = new HashMap<>();
    private final Map<String, Boolean> awaitingAppId = new HashMap<>();

    public ChatService(ConfigService config, KnowledgeBaseService kb, IntentRouterService intent,
                       CardApplicationAndTrxService cardApplicationAndTrxService, OverridesService overrides) {
        this.config = config;
        this.kb = kb;
        this.intent = intent;
        this.cardApplicationAndTrxService = cardApplicationAndTrxService;
        this.overrides = overrides;
    }

    public record ChatResponse(String answer, List<Map<String,String>> citations, String sessionId, boolean canReportMistake) {}

    public ChatResponse respond(String sessionId, String message) {
        if (sessionId == null || sessionId.isBlank()) sessionId = UUID.randomUUID().toString();
        System.out.println("sessionId: " + sessionId + ", message: " + message);

        // 1) Overrides
        System.out.println("Incoming message for override: [" + message + "]");
        var ov = overrides.tryOverride(message);
        System.out.println("tryOverride lookup: [" + message + "]"+" ov "+ov);
        if (ov.isPresent()) {
            return new ChatResponse(ov.get() + "\n\n(✅ This response uses a previously applied fix.)",
                    List.of(), sessionId, true);
        }

        // 2) Transaction follow-up state
        /*if (Boolean.TRUE.equals(awaitingTxId.get(sessionId))) {
            String txId = intent.extractTxId(message);
            if (txId == null) {
                return new ChatResponse("Please share your transaction ID (example: TX12345678).", List.of(), sessionId, true);
            }
            awaitingTxId.put(sessionId, false);
            var tx = cardApplicationAndTrxService.getTransactionStatus(txId);
            if (tx == null) {
                return new ChatResponse("I couldn’t find transaction **" + txId + "**. Please recheck the ID.", List.of(), sessionId, true);
            }
            return new ChatResponse(
                    "Thanks. \nI checked transaction "+tx.getTxId()+".\nStatus: " + tx.getStatus() + " \nUpdated: " + tx.getUpdatedAt(),
                    List.of(), sessionId, true
            );
        }*/

        String directTxID = intent.extractTxId(message);
           if(directTxID != null) {
            var tx = cardApplicationAndTrxService.getTransactionStatus(directTxID);
            if (tx == null) {
                return new ChatResponse("I couldn’t find transaction **" + directTxID + "**. Please recheck the ID.", List.of(), sessionId, true);
            }
            return new ChatResponse(
                    "Thanks. \nI checked transaction "+tx.getTxId()+".\nStatus: " + tx.getStatus() + " \nUpdated: " + tx.getUpdatedAt(),
                    List.of(), sessionId, true
            );
        }

        /*if (Boolean.TRUE.equals(awaitingAppId.get(sessionId))) {
            String appId = intent.extractIdLike(message);
            if(appId == null) {
                return new ChatResponse("Sure — please share your application ID (example: APP1001).", List.of(), sessionId, true);
            }
            awaitingAppId.put(sessionId, false);
            var st = cardApplicationAndTrxService.getApplicationStatus(appId);
            if(st == null) {
                return new ChatResponse("I couldn’t find application **" + appId + "**. Double-check the ID (example: APP1001).",
                        List.of(), sessionId, true);
            }
            String ans = "I checked your card application \nApplication Id: "+st.getApplicationId()+".\nStatus: " + st.getStatus() + "\nUpdated: " + st.getUpdatedAt();
            return new ChatResponse(ans, List.of(), sessionId, true);
        }*/

        String directAppId = intent.extractIdLike(message);
        if (directAppId != null) {
            var app = cardApplicationAndTrxService.getApplicationStatus(directAppId);
            if (app == null) {
                return new ChatResponse("I couldn’t find application **" + directAppId + "**. Double-check the ID (example: APP1001).",
                        List.of(), sessionId, true);
            }
            return new ChatResponse(
                    "Application Id: " + app.getApplicationId() +
                            "\nStatus: " + app.getStatus() +
                            "\nUpdated: " + app.getUpdatedAt(),
                    List.of(), sessionId, true
            );
        }

        // 3) Intent routing
        var detected = intent.detect(message);
        System.out.println("detected: " + detected);

        // 4) TX status and application status
        if (detected == IntentRouterService.Intent.TX_FAILED && !isWhyQuestion(message)) {
            return new ChatResponse("Please share your transaction ID (example: TX12345678).", List.of(), sessionId, true);
        }

        if (detected == IntentRouterService.Intent.APP_STATUS && !isWhyQuestion(message)) {
            return new ChatResponse("Sure — please share your application ID (example: APP1001).", List.of(), sessionId, true);
        }

        // 5) KB search
        var hits = kb.search(message, 3);
        if (!hits.isEmpty() && hits.getFirst().score() >= 2) {
            var top = hits.getFirst();

            String guidelines = config.get("additional_guidelines");
            if(guidelines == null) {
                guidelines = "";
            }

            String ans = top.snippet() +
                    "\n\n"+
                    applyGuideLinesHint(guidelines)+
                    "\n\nSource :\n" +
                    hits.stream()
                            .map(h->"-"+h.title()+" ("+h.url()+")")
                            .toList();

            List<Map<String,String>> cites = hits.stream()
                    .limit(3)
                    .map(h -> Map.of(
                            "title", h.title(),
                            "url", h.url()
                    )).toList();

            return new ChatResponse(ans, cites, sessionId, true);
        }

        return new ChatResponse(
                "I couldn’t find a confident answer in the Atome Card knowledge base. Can you share a bit more detail (what you’re trying to do + what error/message you saw)?",
                List.of(), sessionId, true
        );
    }

    private boolean isWhyQuestion(String message) {
        String m = message.toLowerCase();
        return m.contains("why") || m.contains("reason") || m.contains("how") || m.contains("what happened");
    }

    private String applyGuideLinesHint(String guidelines) {
        // Minimal “behavior change” demonstration:
        // - If guideline says "be concise" => keep a short closing line
        // - If guideline says "ask clarifying" => add a clarifying question line
        String g = guidelines.toLowerCase();

        StringBuilder out = new StringBuilder();

        if (g.contains("polite")) {
            out.append("Thanks for reaching out. ");
        }

        if (g.contains("concise")) {
            out.append("Here’s the key info:\n");
        } else {
            out.append("Here are the details:\n");
        }

        if (g.contains("ask") && g.contains("clarifying")) {
            out.append("\nIf you tell me what you’re trying to do in the app, I can guide you step-by-step.");
        } else {
            out.append("\nIf you want, tell me what you’re trying to do and I’ll guide you step-by-step.");
        }

        return out.toString();
    }
}
package com.atome.bot.service;

import com.atome.bot.model.CardApplication;
import com.atome.bot.model.CardTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ConfigService config;
    @Mock private KnowledgeBaseService kb;
    @Mock private IntentRouterService intent;
    @Mock private CardApplicationAndTrxService cardService;
    @Mock private OverridesService overrides;

    @InjectMocks
    private ChatService service;

    @BeforeEach
    void setupDefaults() {
        when(overrides.tryOverride(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void respond_generatesSessionIdWhenMissing() {
        ChatService.ChatResponse response = service.respond(null, "hello");

        assertNotNull(response.sessionId());
        assertFalse(response.sessionId().isBlank());
        assertTrue(response.canReportMistake());
    }

    @Test
    void respond_returnsOverrideImmediately() {
        when(overrides.tryOverride("same question")).thenReturn(Optional.of("fixed answer"));

        ChatService.ChatResponse response = service.respond("s1", "same question");

        assertTrue(response.answer().contains("fixed answer"));
        assertTrue(response.answer().contains("previously applied fix"));
        verifyNoInteractions(kb);
    }

    @Test
    void respond_returnsTransactionStatusWhenDirectTxIdExists() {
        when(intent.extractTxId("check TX9001")).thenReturn("TX9001");
        when(cardService.getTransactionStatus("TX9001"))
                .thenReturn(new CardTransaction("TX9001", "SUCCESS", Instant.now()));

        ChatService.ChatResponse response = service.respond("s1", "check TX9001");

        assertTrue(response.answer().contains("TX9001"));
        assertTrue(response.answer().contains("SUCCESS"));
    }

    @Test
    void respond_returnsTransactionNotFoundWhenDirectTxIdMissingInStore() {
        when(intent.extractTxId("check TX9999")).thenReturn("TX9999");
        when(cardService.getTransactionStatus("TX9999")).thenReturn(null);

        ChatService.ChatResponse response = service.respond("s1", "check TX9999");

        assertTrue(response.answer().contains("I couldn’t find transaction **TX9999**"));
    }

    @Test
    void respond_returnsApplicationStatusWhenDirectAppIdExists() {
        when(intent.extractIdLike("status APP1001")).thenReturn("APP1001");
        when(cardService.getApplicationStatus("APP1001"))
                .thenReturn(new CardApplication("APP1001", "APPROVED", Instant.now()));

        ChatService.ChatResponse response = service.respond("s1", "status APP1001");

        assertTrue(response.answer().contains("APP1001"));
        assertTrue(response.answer().contains("APPROVED"));
    }

    @Test
    void respond_returnsApplicationNotFoundWhenDirectAppIdMissingInStore() {
        when(intent.extractIdLike("status APP9999")).thenReturn("APP9999");
        when(cardService.getApplicationStatus("APP9999")).thenReturn(null);

        ChatService.ChatResponse response = service.respond("s1", "status APP9999");

        assertTrue(response.answer().contains("I couldn’t find application **APP9999**"));
    }

    @Test
    void respond_promptsForTxIdWhenIntentIsTxFailedAndQuestionIsNotWhy() {
        when(intent.detect("my transaction failed")).thenReturn(IntentRouterService.Intent.TX_FAILED);

        ChatService.ChatResponse response = service.respond("s1", "my transaction failed");

        assertEquals("Please share your transaction ID (example: TX12345678).", response.answer());
    }

    @Test
    void respond_promptsForAppIdWhenIntentIsAppStatusAndQuestionIsNotWhy() {
        when(intent.detect("check application status")).thenReturn(IntentRouterService.Intent.APP_STATUS);

        ChatService.ChatResponse response = service.respond("s1", "check application status");

        assertEquals("Sure — please share your application ID (example: APP1001).", response.answer());
    }

    @Test
    void respond_doesNotPromptForTxIdForWhyQuestion_andFallsBackToKb() {
        when(intent.detect("why transaction failed")).thenReturn(IntentRouterService.Intent.TX_FAILED);
        when(kb.search("why transaction failed", 3)).thenReturn(List.of(
                new KnowledgeBaseService.KbHit("Why tx fails", "u1", "Because of merchant or limits", 5)
        ));
        when(config.get("additional_guidelines")).thenReturn("be polite and concise");

        ChatService.ChatResponse response = service.respond("s1", "why transaction failed");

        assertTrue(response.answer().contains("Because of merchant or limits"));
        assertTrue(response.answer().contains("Thanks for reaching out."));
        assertEquals(1, response.citations().size());
    }

    @Test
    void respond_returnsKbAnswerWithClarifyingGuideline() {
        when(kb.search("refund timeline", 3)).thenReturn(List.of(
                new KnowledgeBaseService.KbHit("Refund timeline", "u1", "Refund takes up to 7 business days", 6),
                new KnowledgeBaseService.KbHit("More help", "u2", "Other details", 3)
        ));
        when(config.get("additional_guidelines")).thenReturn("polite ask clarifying questions");

        ChatService.ChatResponse response = service.respond("s1", "refund timeline");

        assertTrue(response.answer().contains("Refund takes up to 7 business days"));
        assertTrue(response.answer().contains("Thanks for reaching out."));
        assertTrue(response.answer().contains("If you tell me what you’re trying to do in the app, I can guide you step-by-step."));
        assertEquals(2, response.citations().size());
        assertEquals("Refund timeline", response.citations().getFirst().get("title"));
    }

    @Test
    void respond_returnsFallbackWhenKbHasNoConfidentAnswer() {
        ChatService.ChatResponse response = service.respond("s1", "unknown");

        assertTrue(response.answer().contains("I couldn’t find a confident answer"));
    }
}

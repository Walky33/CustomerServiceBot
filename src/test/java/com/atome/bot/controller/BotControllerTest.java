package com.atome.bot.controller;

import com.atome.bot.model.Report;
import com.atome.bot.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotControllerTest {

    @Mock private ConfigService configService;
    @Mock private KnowledgeBaseService knowledgeBaseService;
    @Mock private ChatService chatService;
    @Mock private ReportsService reportsService;
    @Mock private OverridesService overridesService;

    @InjectMocks
    private BotController controller;

    private Report report;

    @BeforeEach
    void setUp() {
        report = new Report("r1", "Where is my refund?", "old", "wrong", "Use card timeline", "OPEN", null, null);
    }

    @Test
    void getConfig_returnsAllConfig() {
        Map<String, String> config = Map.of("kb_url", "u", "additional_guidelines", "g");
        when(configService.getAll()).thenReturn(config);

        assertEquals(config, controller.getConfig());
    }

    @Test
    void setConfig_updatesOnlyProvidedKeys() {
        Map<String, String> body = Map.of("kb_url", "new-url", "additional_guidelines", "be concise");
        Map<String, String> after = Map.of("kb_url", "new-url", "additional_guidelines", "be concise");
        when(configService.getAll()).thenReturn(after);

        Map<String, String> result = controller.setConfig(body);

        verify(configService).set("kb_url", "new-url");
        verify(configService).set("additional_guidelines", "be concise");
        assertEquals(after, result);
    }

    @Test
    void rebuild_returnsOkWhenKbRebuildSucceeds() throws Exception {
        when(configService.get("kb_url")).thenReturn("https://example.zendesk.com/hc/en-gb/categories/123-test");
        when(knowledgeBaseService.rebuild(anyString()))
                .thenReturn(new KnowledgeBaseService.RebuildResult(5, 5));

        ResponseEntity<?> response = controller.rebuild();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("Ok"));
        assertEquals(5, body.get("linksFound"));
        assertEquals(5, body.get("indexed"));
    }

    @Test
    void rebuild_returnsBadRequestForInvalidKbUrl() throws Exception {
        when(configService.get("kb_url")).thenReturn("bad-url");
        when(knowledgeBaseService.rebuild(anyString()))
                .thenThrow(new IllegalArgumentException("kb_url is empty"));

        ResponseEntity<?> response = controller.rebuild();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("ok"));
        assertEquals("INVALID_KB_URL", body.get("error"));
    }

    @Test
    void rebuild_returnsServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        when(configService.get("kb_url")).thenReturn("https://example.zendesk.com/hc/en-gb/categories/123-test");
        when(knowledgeBaseService.rebuild(anyString()))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = controller.rebuild();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("ok"));
        assertEquals("REBUILD_FAILED", body.get("error"));
    }

    @Test
    void chat_delegatesToChatService() {
        ChatService.ChatResponse expected = new ChatService.ChatResponse("answer", List.of(), "s1", true);
        when(chatService.respond("s1", "hello")).thenReturn(expected);

        ChatService.ChatResponse result = controller.chat(Map.of("sessionId", "s1", "message", "hello"));

        assertSame(expected, result);
    }

    @Test
    void report_createsNewReport() {
        when(reportsService.create("q", "a", "bad", "correct")).thenReturn(report);

        Report result = controller.report(Map.of(
                "question", "q",
                "botAnswer", "a",
                "userFeedback", "bad",
                "expectedAnswer", "correct"
        ));

        assertSame(report, result);
    }

    @Test
    void listOpen_returnsOpenReports() {
        List<Report> expected = List.of(report);
        when(reportsService.listOpen()).thenReturn(expected);

        Object result = controller.listOpen();

        assertSame(expected, result);
    }

    @Test
    void autoFix_usesExplicitCorrectAnswer_marksFixed_archives_and_returnsDemoAnswer() {
        when(reportsService.get("r1")).thenReturn(report);
        when(chatService.respond("demo-session", report.getQuestion()))
                .thenReturn(new ChatService.ChatResponse("new answer", List.of(), "demo-session", true));

        Object result = controller.autoFix("r1", Map.of("correctAnswer", "This is corrected"));

        verify(overridesService).createOverride(report.getQuestion(), "This is corrected", "r1");
        verify(reportsService).markFixed("r1");
        verify(reportsService).archive("r1");
        Map<?, ?> body = (Map<?, ?>) result;
        assertEquals(true, body.get("ok"));
        assertEquals("r1", body.get("archivedReportId"));
        assertEquals("new answer", body.get("demoNewAnswer"));
    }

    @Test
    void autoFix_fallsBackToReportExpectedAnswerWhenRequestHasNoCorrectAnswer() {
        when(reportsService.get("r1")).thenReturn(report);
        when(chatService.respond("demo-session", report.getQuestion()))
                .thenReturn(new ChatService.ChatResponse("new answer", List.of(), "demo-session", true));

        controller.autoFix("r1", Map.of());

        verify(overridesService).createOverride(report.getQuestion(), report.getExpectedAnswer(), "r1");
    }

    @Test
    void autoFix_throwsWhenCorrectedAnswerIsMissingAndExpectedAnswerIsBlank() {
        report.setExpectedAnswer("   ");
        when(reportsService.get("r1")).thenReturn(report);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.autoFix("r1", Map.of()));

        assertEquals("correctAnswer is required", ex.getMessage());
        verify(overridesService, never()).createOverride(anyString(), anyString(), anyString());
    }
}

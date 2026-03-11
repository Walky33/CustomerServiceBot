package com.atome.bot.service;

import com.atome.bot.model.KnowledgeBaseDoc;
import com.atome.bot.repositories.KnowledgeBaseDocRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseServiceTest {

    @Mock
    private KnowledgeBaseDocRepository kbRepo;

    @InjectMocks
    private KnowledgeBaseService service;

    @Test
    void rebuild_throwsWhenKbUrlIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.rebuild(" "));
        assertEquals("kb_url is empty", ex.getMessage());
    }

    @Test
    void rebuild_throwsWhenKbUrlDoesNotContainCategoryId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.rebuild("https://example.zendesk.com/hc/en-gb/articles/123"));
        assertTrue(ex.getMessage().contains("kb_url must contain /categories/{id}"));
    }

    @Test
    void search_returnsHitsSortedByScoreAndLimited() {
        when(kbRepo.findAll()).thenReturn(List.of(
                new KnowledgeBaseDoc("1", "u1", "Refund timeline", "Refund takes 7 days from settlement", Instant.now()),
                new KnowledgeBaseDoc("2", "u2", "Card application status", "Application status can be checked in app", Instant.now()),
                new KnowledgeBaseDoc("3", "u3", "Generic", "Nothing related", Instant.now())
        ));

        List<KnowledgeBaseService.KbHit> hits = service.search("refund timeline", 1);

        assertEquals(1, hits.size());
        assertEquals("Refund timeline", hits.getFirst().title());
        assertTrue(hits.getFirst().score() > 0);
    }

    @Test
    void search_returnsEmptyWhenNoTokensMatch() {
        when(kbRepo.findAll()).thenReturn(List.of(
                new KnowledgeBaseDoc("1", "u1", "Title", "Body", Instant.now())
        ));

        assertTrue(service.search("completely unknown phrase", 3).isEmpty());
    }

    @Test
    void search_buildsSnippetUpTo400Chars() {
        String longContent = "x".repeat(500);
        when(kbRepo.findAll()).thenReturn(List.of(
                new KnowledgeBaseDoc("1", "u1", "refund title", longContent, Instant.now())
        ));

        List<KnowledgeBaseService.KbHit> hits = service.search("refund", 3);

        assertEquals(1, hits.size());
        assertTrue(hits.getFirst().snippet().length() <= 401);
        assertTrue(hits.getFirst().snippet().endsWith("…"));
    }
}

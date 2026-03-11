package com.atome.bot.service;

import com.atome.bot.model.Overrides;
import com.atome.bot.repositories.OverrideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OverridesServiceTest {

    @Mock
    private OverrideRepository overrideRepository;

    @InjectMocks
    private OverridesService service;

    @Test
    void tryOverride_returnsCorrectAnswerWhenPresent() {
        when(overrideRepository.findTopByQuestionExactOrderByCreatedAtDesc("q"))
                .thenReturn(Optional.of(new Overrides("1", "q", "correct", null, "r1")));

        Optional<String> result = service.tryOverride("q");

        assertTrue(result.isPresent());
        assertEquals("correct", result.get());
    }

    @Test
    void tryOverride_returnsEmptyWhenNotFound() {
        when(overrideRepository.findTopByQuestionExactOrderByCreatedAtDesc("q"))
                .thenReturn(Optional.empty());

        assertTrue(service.tryOverride("q").isEmpty());
    }

    @Test
    void createOverride_savesOverride() {
        when(overrideRepository.save(any(Overrides.class))).thenAnswer(inv -> inv.getArgument(0));

        Overrides saved = service.createOverride("exact question", "correct answer", "report-1");

        assertNotNull(saved.getId());
        assertEquals("exact question", saved.getQuestionExact());
        assertEquals("correct answer", saved.getCorrectAnswer());
        assertEquals("report-1", saved.getArchivedFromReportId());
        assertNotNull(saved.getCreatedAt());

        ArgumentCaptor<Overrides> captor = ArgumentCaptor.forClass(Overrides.class);
        verify(overrideRepository).save(captor.capture());
        assertEquals("exact question", captor.getValue().getQuestionExact());
    }
}

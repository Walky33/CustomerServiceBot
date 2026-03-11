package com.atome.bot.service;

import com.atome.bot.model.Overrides;
import com.atome.bot.repositories.OverrideRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OverridesService {
    private final OverrideRepository overrideRepository;

    public OverridesService(OverrideRepository overrideRepository) {
        this.overrideRepository = overrideRepository;
    }

    public Optional<String> tryOverride(String exactQuestion) {
        return overrideRepository.findTopByQuestionExactOrderByCreatedAtDesc(exactQuestion)
                .map(Overrides::getCorrectAnswer);
    }

    public Overrides createOverride(String exactQuestion, String correctAnswer, String reportId) {
        Overrides overrides = new Overrides(
                UUID.randomUUID().toString(),
                exactQuestion,
                correctAnswer,
                Instant.now(),
                reportId
        );
        return overrideRepository.save(overrides);
    }
}

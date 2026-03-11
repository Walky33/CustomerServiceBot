package com.atome.bot.repositories;

import com.atome.bot.model.Overrides;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OverrideRepository extends JpaRepository<Overrides,String> {
    Optional<Overrides> findTopByQuestionExactOrderByCreatedAtDesc(String questionExact);
}

package com.atome.bot.repositories;

import com.atome.bot.model.CardApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardApplicationRepository extends JpaRepository<CardApplication,String> {
}

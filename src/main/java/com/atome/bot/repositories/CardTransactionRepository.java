package com.atome.bot.repositories;

import com.atome.bot.model.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardTransactionRepository extends JpaRepository<CardTransaction,String> {
}

package com.atome.bot.service;

import com.atome.bot.model.CardApplication;
import com.atome.bot.model.CardTransaction;
import com.atome.bot.repositories.CardApplicationRepository;
import com.atome.bot.repositories.CardTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CardApplicationAndTrxService {
    private final CardApplicationRepository cardApplicationRepository;
    private final CardTransactionRepository cardTransactionRepository;

    public CardApplicationAndTrxService(CardTransactionRepository cardTransactionRepository,
                                     CardApplicationRepository cardApplicationRepository){
        this.cardApplicationRepository = cardApplicationRepository;
        this.cardTransactionRepository = cardTransactionRepository;
        insertIfEmpty();
    }

    private void insertIfEmpty() {
        if (cardApplicationRepository.count() == 0) {
            cardApplicationRepository.save(new CardApplication("APP1001", "SUBMITTED", Instant.now()));
            cardApplicationRepository.save(new CardApplication("APP1002", "UNDER_REVIEW", Instant.now()));
            cardApplicationRepository.save(new CardApplication("APP1003", "APPROVED", Instant.now()));
            cardApplicationRepository.save(new CardApplication("APP1004", "REJECTED", Instant.now()));
        }
        if (cardTransactionRepository.count() == 0) {
            cardTransactionRepository.save(new CardTransaction("TX9001", "DECLINED", Instant.now()));
            cardTransactionRepository.save(new CardTransaction("TX9002", "PENDING", Instant.now()));
            cardTransactionRepository.save(new CardTransaction("TX9003", "SUCCESS", Instant.now()));
            cardTransactionRepository.save(new CardTransaction("TX9004", "REVERSED", Instant.now()));
        }
    }

    public CardApplication getApplicationStatus(String applicationId) {
        return cardApplicationRepository.findById(applicationId).orElse(null);
    }

    public CardTransaction getTransactionStatus(String txId) {
        return cardTransactionRepository.findById(txId).orElse(null);
    }

    public CardApplication upsertApplication(String applicationId, String status) {
        var e = new CardApplication(applicationId, status, Instant.now());
        return cardApplicationRepository.save(e);
    }

    public CardTransaction upsertTransaction(String txId, String status) {
        var e = new CardTransaction(txId, status, Instant.now());
        return cardTransactionRepository.save(e);
    }

    public List<CardApplication> listApplications() {
        return cardApplicationRepository.findAll();
    }

    public List<CardTransaction> listTransactions() {
        return cardTransactionRepository.findAll();
    }
}

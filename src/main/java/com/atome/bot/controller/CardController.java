package com.atome.bot.controller;

import com.atome.bot.model.CardApplication;
import com.atome.bot.model.CardTransaction;
import com.atome.bot.service.CardApplicationAndTrxService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/card")
public class CardController {
    private final CardApplicationAndTrxService cardApplicationAndTrxService;

    public CardController(CardApplicationAndTrxService cardApplicationAndTrxService) {
        this.cardApplicationAndTrxService = cardApplicationAndTrxService;
    }

    @GetMapping("/applications")
    public List<CardApplication> listApps() {
        return cardApplicationAndTrxService.listApplications();
    }

    @PostMapping("/applications")
    public CardApplication upsertApp(@RequestBody Map<String,String> body) {
        String applicationId = body.get("applicationId");
        String status = body.get("status");
        if (applicationId == null || status == null) throw new IllegalArgumentException("applicationId and status required");
        return cardApplicationAndTrxService.upsertApplication(applicationId, status);
    }

    @GetMapping("/transactions")
    public List<CardTransaction> listTx() {
        return cardApplicationAndTrxService.listTransactions();
    }

    @PostMapping("/transactions")
    public CardTransaction upsertTx(@RequestBody Map<String,String> body) {
        String txId = body.get("txId");
        String status = body.get("status");
        if (txId == null || status == null) throw new IllegalArgumentException("txId and status required");
        return cardApplicationAndTrxService.upsertTransaction(txId, status);
    }
}

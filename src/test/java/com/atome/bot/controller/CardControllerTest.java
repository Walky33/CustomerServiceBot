package com.atome.bot.controller;

import com.atome.bot.model.CardApplication;
import com.atome.bot.model.CardTransaction;
import com.atome.bot.service.CardApplicationAndTrxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardApplicationAndTrxService service;

    @InjectMocks
    private CardController controller;

    @Test
    void listApps_returnsApplications() {
        List<CardApplication> expected = List.of(new CardApplication("APP1001", "SUBMITTED", Instant.now()));
        when(service.listApplications()).thenReturn(expected);

        assertSame(expected, controller.listApps());
    }

    @Test
    void upsertApp_savesApplication() {
        CardApplication expected = new CardApplication("APP2001", "APPROVED", Instant.now());
        when(service.upsertApplication("APP2001", "APPROVED")).thenReturn(expected);

        CardApplication result = controller.upsertApp(Map.of("applicationId", "APP2001", "status", "APPROVED"));

        assertSame(expected, result);
    }

    @Test
    void upsertApp_throwsForMissingFields() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.upsertApp(Map.of("applicationId", "APP2001")));
        assertEquals("applicationId and status required", ex.getMessage());
    }

    @Test
    void listTx_returnsTransactions() {
        List<CardTransaction> expected = List.of(new CardTransaction("TX9001", "DECLINED", Instant.now()));
        when(service.listTransactions()).thenReturn(expected);

        assertSame(expected, controller.listTx());
    }

    @Test
    void upsertTx_savesTransaction() {
        CardTransaction expected = new CardTransaction("TX9009", "SUCCESS", Instant.now());
        when(service.upsertTransaction("TX9009", "SUCCESS")).thenReturn(expected);

        CardTransaction result = controller.upsertTx(Map.of("txId", "TX9009", "status", "SUCCESS"));

        assertSame(expected, result);
    }

    @Test
    void upsertTx_throwsForMissingFields() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.upsertTx(Map.of("txId", "TX9009")));
        assertEquals("txId and status required", ex.getMessage());
    }
}

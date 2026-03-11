package com.atome.bot.service;

import com.atome.bot.model.CardApplication;
import com.atome.bot.model.CardTransaction;
import com.atome.bot.repositories.CardApplicationRepository;
import com.atome.bot.repositories.CardTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardApplicationAndTrxServiceTest {

    @Mock private CardApplicationRepository appRepository;
    @Mock private CardTransactionRepository txRepository;

    @Test
    void constructor_seedsDefaultDataWhenRepositoriesAreEmpty() {
        when(appRepository.count()).thenReturn(0L);
        when(txRepository.count()).thenReturn(0L);

        new CardApplicationAndTrxService(txRepository, appRepository);

        verify(appRepository, times(4)).save(any(CardApplication.class));
        verify(txRepository, times(4)).save(any(CardTransaction.class));
    }

    @Test
    void constructor_doesNotSeedWhenDataAlreadyExists() {
        when(appRepository.count()).thenReturn(2L);
        when(txRepository.count()).thenReturn(1L);

        new CardApplicationAndTrxService(txRepository, appRepository);

        verify(appRepository, never()).save(any(CardApplication.class));
        verify(txRepository, never()).save(any(CardTransaction.class));
    }

    @Test
    void getApplicationStatus_returnsEntityOrNull() {
        when(appRepository.count()).thenReturn(1L);
        when(txRepository.count()).thenReturn(1L);
        CardApplicationAndTrxService service = new CardApplicationAndTrxService(txRepository, appRepository);
        CardApplication app = new CardApplication();
        when(appRepository.findById("APP1001")).thenReturn(Optional.of(app));
        when(appRepository.findById("APP9999")).thenReturn(Optional.empty());

        assertSame(app, service.getApplicationStatus("APP1001"));
        assertNull(service.getApplicationStatus("APP9999"));
    }

    @Test
    void getTransactionStatus_returnsEntityOrNull() {
        when(appRepository.count()).thenReturn(1L);
        when(txRepository.count()).thenReturn(1L);
        CardApplicationAndTrxService service = new CardApplicationAndTrxService(txRepository, appRepository);
        CardTransaction tx = new CardTransaction();
        when(txRepository.findById("TX9001")).thenReturn(Optional.of(tx));
        when(txRepository.findById("TX9999")).thenReturn(Optional.empty());

        assertSame(tx, service.getTransactionStatus("TX9001"));
        assertNull(service.getTransactionStatus("TX9999"));
    }

    @Test
    void upsertApplication_savesEntityWithUpdatedTimestamp() {
        when(appRepository.count()).thenReturn(1L);
        when(txRepository.count()).thenReturn(1L);
        when(appRepository.save(any(CardApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        CardApplicationAndTrxService service = new CardApplicationAndTrxService(txRepository, appRepository);

        CardApplication result = service.upsertApplication("APP2001", "APPROVED");

        assertEquals("APP2001", result.getApplicationId());
        assertEquals("APPROVED", result.getStatus());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void upsertTransaction_savesEntityWithUpdatedTimestamp() {
        when(appRepository.count()).thenReturn(1L);
        when(txRepository.count()).thenReturn(1L);
        when(txRepository.save(any(CardTransaction.class))).thenAnswer(inv -> inv.getArgument(0));
        CardApplicationAndTrxService service = new CardApplicationAndTrxService(txRepository, appRepository);

        CardTransaction result = service.upsertTransaction("TX9010", "SUCCESS");

        assertEquals("TX9010", result.getTxId());
        assertEquals("SUCCESS", result.getStatus());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void listMethods_delegateToRepositories() {
        when(appRepository.count()).thenReturn(1L);
        when(txRepository.count()).thenReturn(1L);
        CardApplicationAndTrxService service = new CardApplicationAndTrxService(txRepository, appRepository);
        List<CardApplication> apps = List.of(new CardApplication());
        List<CardTransaction> txs = List.of(new CardTransaction());
        when(appRepository.findAll()).thenReturn(apps);
        when(txRepository.findAll()).thenReturn(txs);

        assertSame(apps, service.listApplications());
        assertSame(txs, service.listTransactions());
    }
}

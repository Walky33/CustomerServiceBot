package com.atome.bot.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntentRouterServiceTest {

    private final IntentRouterService service = new IntentRouterService();

    @Test
    void detect_returnsAppStatus() {
        assertEquals(IntentRouterService.Intent.APP_STATUS,
                service.detect("Can you check application status for me?"));
    }

    @Test
    void detect_returnsTxFailed() {
        assertEquals(IntentRouterService.Intent.TX_FAILED,
                service.detect("My transaction failed yesterday"));
    }

    @Test
    void detect_returnsGeneral() {
        assertEquals(IntentRouterService.Intent.GENERAL,
                service.detect("How do I update my profile details?"));
    }

    @Test
    void extractTxId_returnsMatchedValue() {
        assertEquals("Tx9001", service.extractTxId("Please check Tx9001 now"));
    }

    @Test
    void extractTxId_returnsNullWhenAbsent() {
        assertNull(service.extractTxId("There is no id here"));
    }

    @Test
    void extractIdLike_returnsUppercaseAppId() {
        assertEquals("APP1001", service.extractIdLike("status for app1001"));
    }

    @Test
    void extractIdLike_returnsNullWhenAbsent() {
        assertNull(service.extractIdLike("status for application tomorrow"));
    }
}

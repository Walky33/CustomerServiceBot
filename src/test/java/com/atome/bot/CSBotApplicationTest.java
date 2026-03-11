package com.atome.bot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CSBotApplicationTest {

    @Test
    void main_methodIsCallable() {
        assertDoesNotThrow(() -> CSBotApplication.main(new String[]{}));
    }
}

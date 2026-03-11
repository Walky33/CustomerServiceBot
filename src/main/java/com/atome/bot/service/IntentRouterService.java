package com.atome.bot.service;

import org.springframework.stereotype.Service;

@Service
public class IntentRouterService {
    public enum Intent { APP_STATUS, TX_FAILED, GENERAL }

    public Intent detect(String msg) {
        String m = msg.toLowerCase();
        System.out.println("detect msg::"+msg);

        boolean app = m.contains("application status") ||
                m.contains("check application") ||
                (m.contains("application") && m.contains("status"));

        if (app) return Intent.APP_STATUS;

        boolean tx = m.contains("transaction status") ||
                m.contains("check transaction") ||
                m.contains("check my transaction") ||
                m.contains("my transaction failed") ||
                m.contains("transaction failed") ||
                m.contains("failed transaction");

        if (tx) return Intent.TX_FAILED;

        return Intent.GENERAL;
    }

    public String extractTxId(String msg) {
        var p = java.util.regex.Pattern.compile("\\b(TX[0-9A-Z]{3,})\\b", java.util.regex.Pattern.CASE_INSENSITIVE);
        System.out.println(p);
        var m = p.matcher(msg);
        return m.find() ? m.group(1) : null;
    }

    public String extractIdLike(String msg) {
        var p = java.util.regex.Pattern.compile("\\b(APP\\d{3,})\\b", java.util.regex.Pattern.CASE_INSENSITIVE);
        var m = p.matcher(msg);
        return m.find() ? m.group(1).toUpperCase() : null;
    }

}

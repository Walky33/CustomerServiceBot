package com.atome.bot.controller;

import com.atome.bot.model.Report;
import com.atome.bot.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BotController {
    private final ConfigService config;
    private final KnowledgeBaseService kb;
    private final ChatService chat;
    private final ReportsService reports;
    private final OverridesService overrides;

    public BotController(ConfigService config,
                         KnowledgeBaseService kb,
                         ChatService chat,
                         ReportsService reports,
                         OverridesService overrides) {
        this.config = config;
        this.kb = kb;
        this.chat = chat;
        this.reports = reports;
        this.overrides = overrides;
    }

    @GetMapping("/config")
    public Map<String,String> getConfig() {
        return config.getAll();
    }

    @PostMapping("/config")
    public Map<String,String> setConfig(@RequestBody Map<String,String> body) {
        if (body.containsKey("kb_url")) config.set("kb_url", body.get("kb_url"));
        if (body.containsKey("additional_guidelines")) config.set("additional_guidelines", body.get("additional_guidelines"));
        return config.getAll();
    }

    @PostMapping("/kb/rebuild")
    public ResponseEntity<?> rebuild() throws Exception {
        String kbUrl = config.get("kb_url");
        try {
            var result = kb.rebuild(kbUrl);
            return ResponseEntity.ok(Map.of("Ok",true,"kbUrl",kbUrl,"linksFound",result.linksFound(),"indexed",result.indexed()));
        }catch(IllegalArgumentException ie){
            ie.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "kbUrl", kbUrl,
                    "error", "INVALID_KB_URL",
                    "message", ie.getMessage()
            ));
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "ok", false,
                    "kbUrl", kbUrl,
                    "error", "REBUILD_FAILED",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/chat")
    public ChatService.ChatResponse chat(@RequestBody Map<String,String> body) {
        return chat.respond(body.get("sessionId"), body.get("message"));
    }

    @PostMapping("/report")
    public Report report(@RequestBody Map<String,String> body) {
        return reports.create(
                body.get("question"),
                body.get("botAnswer"),
                body.get("userFeedback"),
                body.getOrDefault("expectedAnswer", null)
        );
    }

    @GetMapping("/reports/open")
    public Object listOpen() {
        return reports.listOpen();
    }

    @PostMapping("/reports/{id}/autofix")
    public Object autoFix(@PathVariable("id") String id, @RequestBody Map<String,String> body) {
        // corrected answer comes from UI (or from expectedAnswer)
        Report r = reports.get(id);
        String corrected = body.getOrDefault("correctAnswer", r.getExpectedAnswer());
        if (corrected == null || corrected.isBlank()) {
            throw new IllegalArgumentException("correctAnswer is required");
        }

        overrides.createOverride(r.getQuestion(), corrected, id);
        reports.markFixed(id);
        reports.archive(id);

        // Demonstrate fix: ask the same question again
        var demo = chat.respond("demo-session", r.getQuestion());

        return Map.of(
                "ok", true,
                "archivedReportId", id,
                "demoNewAnswer", demo.answer()
        );
    }
}

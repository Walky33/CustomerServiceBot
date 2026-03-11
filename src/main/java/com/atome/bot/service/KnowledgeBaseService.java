package com.atome.bot.service;

import com.atome.bot.model.KnowledgeBaseDoc;
import com.atome.bot.repositories.KnowledgeBaseDocRepository;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {
    private final KnowledgeBaseDocRepository kbRepo;
    private static final Set<String> STOPWORDS = Set.of(
            "what","is","are","the","for","a","an","of","to","and","in","on","my","your","me","please"
    );

    public KnowledgeBaseService(KnowledgeBaseDocRepository kbRepo) {
        this.kbRepo = kbRepo;
    }

    public record KbHit(String title, String url, String snippet, int score) {}

    public record RebuildResult(int linksFound, int indexed) {}

    public RebuildResult rebuild(String kbUrl) throws Exception {
        if (kbUrl == null || kbUrl.isBlank()) {
            throw new IllegalArgumentException("kb_url is empty");
        }

        // categoryId extracted from URL
        var m = java.util.regex.Pattern
                .compile("/categories/(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(kbUrl);

        if (!m.find()) {
            throw new IllegalArgumentException(
                    "kb_url must contain /categories/{id}. Example: .../categories/4439682039065-..."
            );
        }

        String categoryId = m.group(1);
        System.out.println(categoryId);

        // Build API base dynamically from URL
        URI uri = URI.create(kbUrl);
        String base = uri.getScheme() + "://" + uri.getHost() + "/api/v2/help_center/en-gb";

        RestTemplate rt = new RestTemplate();

        // Clear old KB docs (so modification reflects correctly)
        kbRepo.deleteAll();

        // Fetch sections
        Map sectionsResp = rt.getForObject(base + "/categories/" + categoryId + "/sections.json", Map.class);

        if (sectionsResp == null || sectionsResp.get("sections") == null) {
            throw new IllegalStateException("No sections found for category: " + categoryId);
        }

        List<Map<String,Object>> sections = (List<Map<String,Object>>) sectionsResp.get("sections");

        int indexed = 0;
        int totalArticles = 0;

        for (Map<String,Object> section : sections) {
            Number sectionId = (Number) section.get("id");

            Map articlesResp = rt.getForObject(base + "/sections/" + sectionId + "/articles.json", Map.class);

            if (articlesResp == null || articlesResp.get("articles") == null) continue;

            List<Map<String,Object>> articles = (List<Map<String,Object>>) articlesResp.get("articles");
            totalArticles += articles.size();

            for (Map<String,Object> a : articles) {
                String url = (String) a.get("html_url");
                String title = (String) a.get("title");
                String body = (String) a.get("body"); // HTML body

                String text = Jsoup.parse(body == null ? "" : body).text();

                KnowledgeBaseDoc entity = new KnowledgeBaseDoc(
                        url, url, title, text, Instant.now()
                );
                kbRepo.save(entity);
                indexed++;
            }
        }

        return new RebuildResult(totalArticles, indexed);
    }

    public List<KbHit> search(String query, int limit) {
        String q = query.toLowerCase();
        List<KnowledgeBaseDoc> docs = kbRepo.findAll();

        List<KbHit> hits = new ArrayList<>();
        for (KnowledgeBaseDoc d : docs) {
            int score = score(q, d.getTitle(), d.getContent());
            if (score > 0) {
                String snippet = d.getContent().length() > 400 ? d.getContent().substring(0, 400) + "…" : d.getContent();
                hits.add(new KbHit(d.getTitle(), d.getUrl(), snippet, score));
            }
        }

        return hits.stream()
                .sorted((a,b) -> Integer.compare(b.score(), a.score()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private int score(String q, String title, String content) {
        int score = 0;
        String t = (title == null ? "" : title).toLowerCase();
        String c = (content == null ? "" : content).toLowerCase();

        for (String token : q.split("\\s+")) {
            token = token.trim();
            if (token.isBlank()) continue;
            if (t.contains(token)) score += 8;
            if (c.contains(token)) score += 2;
        }
        return score;
    }
}

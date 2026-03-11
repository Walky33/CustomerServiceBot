package com.atome.bot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "kb_docs")
public class KnowledgeBaseDoc {
    @Id
    private String id; // use URL as ID
    private String url;
    private String title;

    @Lob
    @Column(length = 2_000_000)
    private String content;

    private Instant updatedAt;
}

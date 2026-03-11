package com.atome.bot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name="reports")
public class Report {
    @Id
    private String id;

    @Lob
    private String question;

    @Lob
    private String botAnswer;

    @Lob
    private String userFeedback;

    @Lob
    private String expectedAnswer; // optional

    private String status; // OPEN/FIXED/ARCHIVED
    private Instant createdAt;
    private Instant resolvedAt;
}

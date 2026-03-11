package com.atome.bot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name="overrides")
public class Overrides {
    @Id
    private String id;

    @Lob
    private String questionExact;

    @Lob
    private String correctAnswer;

    private Instant createdAt;
    private String archivedFromReportId;
}

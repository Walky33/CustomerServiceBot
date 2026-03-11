package com.atome.bot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "card_applications")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor
public class CardApplication {
    @Id
    private String applicationId;
    private String status;
    private Instant updatedAt;
}
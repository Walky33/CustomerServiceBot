package com.atome.bot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "card_transactions")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
public class CardTransaction {
    @Id
    private String txId;
    private String status;
    private Instant updatedAt;
}

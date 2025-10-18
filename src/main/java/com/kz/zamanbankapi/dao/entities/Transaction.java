package com.kz.zamanbankapi.dao.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_card_id", nullable = false)
    private Card senderCard;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_card_id", nullable = false)
    private Card receiverCard;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "report")
    private String report;

    private LocalDateTime createdAt = LocalDateTime.now();
}


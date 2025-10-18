package com.kz.zamanbankapi.dao.entities;

import com.kz.zamanbankapi.dao.enums.AccountType;
import com.kz.zamanbankapi.dao.enums.CardType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private String cvv;

    private CardType cardType;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private String currency;

    private AccountType accountType;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    private String designImageUrl;
}


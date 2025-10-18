package com.kz.zamanbankapi.dao.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "preferences")
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String riskTolerance; // e.g., LOW, MEDIUM, HIGH
    private String investmentPreference; // e.g., STOCKS, BONDS, REAL_ESTATE
    private String communicationChannel; // e.g., EMAIL, SMS, PUSH_NOTIFICATION
    private String financialTipFrequency; // e.g., DAILY, WEEKLY, MONTHLY
}

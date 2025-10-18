package com.kz.zamanbankapi.dao.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Report {
    @Id
    private Long id;
    private String title;
    private String content;
    @ManyToOne
    private User user;
}

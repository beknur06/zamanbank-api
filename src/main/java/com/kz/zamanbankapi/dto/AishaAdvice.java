package com.kz.zamanbankapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
public class AishaAdvice {
    private int mood = 1;
    private String advice;
    public AishaAdvice(String advice, int mood) {
        this.advice = advice;
        this.mood = mood;
    }
}

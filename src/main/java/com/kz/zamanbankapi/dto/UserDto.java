package com.kz.zamanbankapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private String username;
    private String password;
    private List<CardDto> cards;
}

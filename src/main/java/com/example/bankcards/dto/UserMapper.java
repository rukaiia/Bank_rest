package com.example.bankcards.dto;

import com.example.bankcards.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public UserDto convertToDto(User user) {
        List<CardDto> cards = user.getCards().stream()
                .map(card -> CardDto.builder()
                        .id(card.getId())
                        .maskedNumber(card.getMaskedNumber())
                        .expiryDate(card.getExpiryDate())
                        .balance(card.getBalance())
                        .status(card.getStatus())
                        .build())
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .cards(cards)
                .build();
    }

}

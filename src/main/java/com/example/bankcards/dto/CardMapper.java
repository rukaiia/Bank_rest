package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public CardDto toDto(Card card) {
        return CardDto.builder()
                .id(card.getId())
                .maskedNumber(mask(card.getMaskedNumber()))
                .balance(card.getBalance())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .build();
    }

    private String mask(String number) {
        String last4 = number.substring(number.length() - 4);
        return "**** **** **** " + last4;
    }
}



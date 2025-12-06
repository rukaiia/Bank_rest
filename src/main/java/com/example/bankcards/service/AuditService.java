package com.example.bankcards.service;

import com.example.bankcards.entity.CardStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditService {

    public void logLogin(String username, boolean success) {
        log.info("Login attempt: user={}, success={}", username, success);
    }

    public void logAction(String action) {
        log.info("Action: {}", action);
    }

    public void logCardStatusChange(String cardNumber, CardStatus oldStatus, CardStatus newStatus) {
        log.info("Card status changed: card={}, {} -> {}", maskCardNumber(cardNumber), oldStatus, newStatus);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}

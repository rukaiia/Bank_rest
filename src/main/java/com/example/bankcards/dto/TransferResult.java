package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferResult {
    private Card updatedCard;
    private String fromMasked;
    private String toMasked;
    private BigDecimal amount;


}
package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TransferRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private String idempotencyKey;

}


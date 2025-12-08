package com.example.bankcards.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class TransferHistoryDto {
    Long id;
    String from;
    String to;
    BigDecimal amount;
    String status;
    LocalDateTime createdAt;
}


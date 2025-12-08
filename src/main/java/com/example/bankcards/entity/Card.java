package com.example.bankcards.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

@Entity
@Table(name = "cards" , schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encrypted_number", nullable = false, unique = true)
    private String encryptedNumber;

    @Column(name = "masked_number", nullable = false)
    private String maskedNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonBackReference
    private User owner;


    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    @Version
    private Long version;

    public String getNumberDecoded() {
        return new String(Base64.getDecoder().decode(encryptedNumber));
    }
}


package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.stream.Stream;

public interface CardRepository extends JpaRepository<Card, Long> {
    Stream<Card> streamAllBy();

    Stream<Card> streamAllById(Long prefixId);

    Page<Card> findByMaskedNumberContainingIgnoreCase(String maskedNumber, Pageable pageable);
    @EntityGraph(attributePaths = {"owner"})
    @Query("""
    SELECT c FROM Card c
    WHERE (:status IS NULL OR c.status = :status)
      AND (:expiryBefore IS NULL OR c.expiryDate <= :expiryBefore)
      AND (:last4 IS NULL OR FUNCTION('RIGHT', c.encryptedNumber, 4) = :last4)
""")
    Page<Card> findFiltered(
            @Param("status") CardStatus status,
            @Param("expiryBefore") LocalDate expiryBefore,
            @Param("last4") String last4,
            Pageable pageable
    );
}

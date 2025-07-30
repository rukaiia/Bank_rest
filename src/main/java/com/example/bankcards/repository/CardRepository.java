package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.stream.Stream;

public interface CardRepository extends JpaRepository<Card, Long> {
    Stream<Card> streamAllBy();

    Stream<Card> streamAllById(Long prefixId);

    Page<Card> findByMaskedNumberContainingIgnoreCase(String maskedNumber, Pageable pageable);
}

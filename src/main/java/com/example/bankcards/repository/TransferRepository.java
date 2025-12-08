package com.example.bankcards.repository;

import com.example.bankcards.entity.TransferEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<TransferEntity, Long> {


    @Query("""
    SELECT t FROM TransferEntity t
    WHERE (:cardId IS NULL OR t.fromCard.id = :cardId OR t.toCard.id = :cardId)
      AND (:start IS NULL OR t.createdAt >= :start)
      AND (:end IS NULL OR t.createdAt <= :end)
      AND (:type IS NULL 
           OR (:type = 'TOP_UP' AND t.fromCard IS NULL) 
           OR (:type = 'TRANSFER' AND t.fromCard IS NOT NULL))
    """)
    Page<TransferEntity> findHistory(
            @Param("cardId") Long cardId,
            @Param("type") String type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );


    Page<TransferEntity> findAll(Specification<TransferEntity> spec, Pageable pageable);

    boolean existsByIdempotencyKey(String idempotencyKey);
    Optional<TransferEntity> findByIdempotencyKey(String idempotencyKey);

}




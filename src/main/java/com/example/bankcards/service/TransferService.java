package com.example.bankcards.service;


import com.example.bankcards.dto.TransferHistoryDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.TransferEntity;
import com.example.bankcards.repository.TransferRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService {

    private final TransferRepository transferRepository;

    public TransferService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    public Page<TransferHistoryDto> getHistory(Long cardId, String type,
                                               LocalDateTime start, LocalDateTime end,
                                               int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<TransferEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cardId != null) {
                predicates.add(cb.or(
                        cb.equal(root.get("fromCard").get("id"), cardId),
                        cb.equal(root.get("toCard").get("id"), cardId)
                ));
            }

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }

            if (type != null) {
                if (type.equalsIgnoreCase("TOP_UP")) {
                    predicates.add(cb.isNull(root.get("fromCard")));
                } else if (type.equalsIgnoreCase("TRANSFER")) {
                    predicates.add(cb.isNotNull(root.get("fromCard")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TransferEntity> entities = transferRepository.findAll(spec, pageable);

        List<TransferHistoryDto> dtos = entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, entities.getTotalElements());
    }


    private TransferHistoryDto toDto(TransferEntity t) {
        return TransferHistoryDto.builder()
                .from(t.getFromCard() != null ? mask(t.getFromCard().getMaskedNumber()) : null)
                .to(t.getToCard() != null ? mask(t.getToCard().getMaskedNumber()) : null)
                .amount(t.getAmount())
                .status(t.getFromCard() != null ? "TRANSFER" : "TOP_UP")
                .createdAt(t.getCreatedAt())
                .build();
    }

    private String mask(String number) {
        return "**** **** **** " + number.substring(number.length() - 4);
    }
}




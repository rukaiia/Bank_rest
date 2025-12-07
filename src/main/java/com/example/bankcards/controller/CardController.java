package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.AuditService;
import com.example.bankcards.service.CardService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardController {

   private final CardService cardService;
   private final CardRepository cardRepository;
   private final CardMapper cardMapper;
    private RateLimiter cardRateLimiter = RateLimiter.create(1.0);
    private AuditService auditService;



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/cards")
    public ResponseEntity<?> getCards(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryBefore,
            @RequestParam(required = false) String last4) {

        if (!cardRateLimiter.tryAcquire()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Слишком частые запросы к картам. Попробуйте позже.");
        }

        Page<CardDto> cards = cardService.findAllFiltered(page, size, sort, status, expiryBefore, last4);

        auditService.logAction("Admin accessed card list");

        return ResponseEntity.ok(cards);
    }


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') ")
    @GetMapping("/api/card")
    public Page<CardDto> getCard(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardPage;

        if (search != null && !search.isEmpty()) {
            cardPage = cardRepository.findByMaskedNumberContainingIgnoreCase(search, pageable);
        } else {
            cardPage = cardRepository.findAll(pageable);
        }

        return cardPage.map(cardMapper::toDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/create")

    public ResponseEntity<CardDto> createCard(@RequestParam Long userId) {
        Card card = cardService.createCard(userId);
        CardDto dto = cardMapper.toDto(card);
        return ResponseEntity.ok(dto);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/card/{id}/number")
    public Map<String, String> getFullNumber(@PathVariable Long id) {
        Card card = getCardOrThrowException(id);

        return Map.of("number", card.getNumberDecoded());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/delete/{card_id}")
    public AckDto deleteCard(@PathVariable("card_id") Long cardId){

    Card card = getCardOrThrowException(cardId);
        cardRepository.delete(card);
        return AckDto.builder().answer(true).build();


    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/block/{card_id}")
    public AckDto block(@PathVariable("card_id") Long cardId){

        Card card = getCardOrThrowException(cardId);
        if (card.getStatus() == CardStatus.BLOCKED){
            throw new RuntimeException("card already blocked");
        }
        CardStatus oldstatus = card.getStatus();
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.saveAndFlush(card);
        auditService.logCardStatusChange(card.getMaskedNumber(), oldstatus, CardStatus.BLOCKED);
        return AckDto.builder().answer(true).build();


    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/unblock/{card_id}")
    public AckDto unblock(@PathVariable("card_id") Long cardId){

        Card card = getCardOrThrowException(cardId);
        if (card.getStatus() == CardStatus.ACTIVE){
            throw new RuntimeException("card already active");
        }
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.saveAndFlush(card);
        return AckDto.builder().answer(true).build();


    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/api/card/{card_id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long card_id){
        Card card = getCardOrThrowException(card_id);
        return ResponseEntity.ok(card.getBalance());
    }
    @PreAuthorize("hasRole('ADMIN')")

    @PatchMapping("/api/cards/{cardId}/top-up")
    public ResponseEntity<CardDto> topUpBalance(
            @PathVariable Long cardId,
            @RequestBody TopUpRequest request
    ) {
        CardDto updatedCard = cardService.topUpBalance(cardId, request.getAmount());
        auditService.logTopUp(updatedCard.getMaskedNumber(),request.getAmount() );
        return ResponseEntity.ok(updatedCard);
    }

    @PreAuthorize("hasRole('USER')")

        @PostMapping("/api/cards/transfer")
        public ResponseEntity<CardDto> transfer(@RequestBody TransferRequest request) {
            TransferResult updatedCard = cardService.transferBetweenCards(request);
            auditService.logTransfer(updatedCard.getFromMasked(),
                    updatedCard.getToMasked(),
                    request.getAmount());
            return ResponseEntity.ok(cardMapper.toDto(updatedCard.getUpdatedCard()));
        }



    private Card getCardOrThrowException(Long cardId) {

        return cardRepository
                .findById(cardId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Card with \"%s\" id doesn't exist.",
                                        cardId
                                )
                        )
                );
    }
//admin    eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluMiIsImlhdCI6MTc2NDI3MTYyNiwiZXhwIjoxNzY0MzU4MDI2fQ.DD5fvUu4qubQ917esE7Ghn-fy5Zuuf5z8xN0QP-lcSU
//user    eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1VTRVIiXSwic3ViIjoianVzdFVzZXIiLCJpYXQiOjE3NjQyNzE4ODAsImV4cCI6MTc2NDM1ODI4MH0.f479cFdLZgvY65JYxGc_3zaOp8LLCI_RQN2XtEtw9Dg

}

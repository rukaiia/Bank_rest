package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/cards")
    public List<CardDto> getAllCards() {
        return cardRepository.streamAllBy()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
    }
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') ")
    @GetMapping("/api/card")
    public Page<CardDto> getcard(
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
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.saveAndFlush(card);
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
    @PreAuthorize("hasRole('USER')")

    @PatchMapping("/api/cards/{cardId}/top-up")
    public ResponseEntity<CardDto> topUpBalance(
            @PathVariable Long cardId,
            @RequestBody TopUpRequest request
    ) {
        CardDto updatedCard = cardService.topUpBalance(cardId, request.getAmount());
        return ResponseEntity.ok(updatedCard);
    }

    @PreAuthorize("hasRole('USER')")

        @PostMapping("/api/cards/transfer")
        public ResponseEntity<CardDto> transfer(@RequestBody TransferRequest request) {
            Card updatedCard = cardService.transferBetweenCards(request);
            return ResponseEntity.ok(cardMapper.toDto(updatedCard));
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


}

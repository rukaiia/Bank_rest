package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

 private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
public Card createCard(Long userId){
   User userCard = userRepository.findById(userId)
           .orElseThrow(() -> new RuntimeException("пользователь с таким id не найден"));
   String   rawNumber = generateCardNumber();
    String encryptedNumber = encryptCardNumber(rawNumber);
    String maskedNumber = maskCardNumber(rawNumber);
    Card card = Card.builder()
            .owner(userCard)
            .status(CardStatus.ACTIVE)
            .balance(BigDecimal.ZERO)
            .expiryDate(LocalDate.now().plusYears(3))
            .encryptedNumber(encryptedNumber)
            .maskedNumber(maskedNumber)
            .build();
   return cardRepository.saveAndFlush(card);


}
    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String maskCardNumber(String number) {
        return "**** **** **** " + number.substring(number.length() - 4);
    }

    private String encryptCardNumber(String number) {
        return Base64.getEncoder().encodeToString(number.getBytes());
    }


    public CardDto topUpBalance(Long cardId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта с id " + cardId + " не найдена"));

        if (card.getOwner().getStatus() == UserStatus.BLOCKED) {
            throw new IllegalArgumentException("Пользователь заблокирован, вы не можете пополнить баланс!");
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта заблокирована и не может быть пополнена");
        }

        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);

        return cardMapper.toDto(card);


    }


        @Transactional
        public Card transferBetweenCards(TransferRequest request) {
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Сумма перевода должна быть положительной");
            }

            Card fromCard = cardRepository.findById(request.getFromCardId())
                    .orElseThrow(() -> new RuntimeException("Карта отправителя не найдена"));

            Card toCard = cardRepository.findById(request.getToCardId())
                    .orElseThrow(() -> new RuntimeException("Карта получателя не найдена"));

            if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
                throw new IllegalArgumentException("Недостаточно средств на карте отправителя");
            }

            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            return fromCard;
        }
    }



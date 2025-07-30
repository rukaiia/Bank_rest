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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateCard_success() {
        User user = new User();
        user.setId(1L);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.saveAndFlush(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card card = cardService.createCard(1L);

        assertNotNull(card);
        assertEquals(user, card.getOwner());
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertEquals(BigDecimal.ZERO, card.getBalance());
        assertTrue(card.getMaskedNumber().startsWith("****"));
    }

    @Test
    void testCreateCard_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cardService.createCard(1L));
        assertEquals("пользователь с таким id не найден", ex.getMessage());
    }

    @Test
    void testTopUpBalance_success() {
        Long cardId = 10L;
        BigDecimal amount = BigDecimal.valueOf(1000);

        User user = new User();
        user.setStatus(UserStatus.ACTIVE);

        Card card = new Card();
        card.setId(cardId);
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        CardDto cardDto = new CardDto();
        cardDto.setId(cardId);
        cardDto.setBalance(amount);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);
        when(cardRepository.save(card)).thenReturn(card);

        CardDto result = cardService.topUpBalance(cardId, amount);

        assertEquals(amount, result.getBalance());
        verify(cardRepository).save(card);
    }

    @Test
    void testTopUpBalance_userBlocked() {
        User user = new User();
        user.setStatus(UserStatus.BLOCKED);

        Card card = new Card();
        card.setOwner(user);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cardService.topUpBalance(1L, BigDecimal.TEN));
        assertEquals("Пользователь заблокирован, вы не можете пополнить баланс!", ex.getMessage());
    }

    @Test
    void testTopUpBalance_cardBlocked() {
        User user = new User();
        user.setStatus(UserStatus.ACTIVE);

        Card card = new Card();
        card.setOwner(user);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(card));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> cardService.topUpBalance(1L, BigDecimal.TEN));
        assertEquals("Карта заблокирована и не может быть пополнена", ex.getMessage());
    }

    @Test
    void testTransferBetweenCards_success() {
        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        fromCard.setStatus(CardStatus.ACTIVE);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setStatus(CardStatus.ACTIVE);

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(200));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.transferBetweenCards(request);

        assertEquals(BigDecimal.valueOf(800), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700), toCard.getBalance());
        assertEquals(fromCard.getId(), result.getId());
    }

    @Test
    void testTransferBetweenCards_insufficientFunds() {
        Card fromCard = new Card();
        fromCard.setBalance(BigDecimal.valueOf(100));

        Card toCard = new Card();
        toCard.setBalance(BigDecimal.valueOf(100));

        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(200));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cardService.transferBetweenCards(request));
        assertEquals("Недостаточно средств на карте отправителя", ex.getMessage());
    }
}


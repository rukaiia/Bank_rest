package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.dto.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
public class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private CardMapper cardMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private UserRepository userRepository;

    private CardDto sampleCardDto;

    @BeforeEach
    public void setup() {
        sampleCardDto = new CardDto();
        sampleCardDto.setId(1L);
        sampleCardDto.setBalance(BigDecimal.valueOf(1000));
        sampleCardDto.setStatus(CardStatus.ACTIVE);
        sampleCardDto.setMaskedNumber("**** ** ** 1234");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateCard() throws Exception {
        Card card = new Card();
        card.setId(1L);
        card.setMaskedNumber("**** ** ** 1234");

        when(cardService.createCard(eq(1L))).thenReturn(card);
        when(cardMapper.toDto(eq(card))).thenReturn(sampleCardDto);

        mockMvc.perform(post("/api/create")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(sampleCardDto.getId()))
                .andExpect(jsonPath("$.maskedNumber").value(sampleCardDto.getMaskedNumber()));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testTopUpBalance() throws Exception {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(BigDecimal.valueOf(500));

        when(cardService.topUpBalance(eq(1L), any())).thenReturn(sampleCardDto);

        mockMvc.perform(patch("/api/cards/1/top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedNumber").value("**** ** ** 1234"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testTransferBetweenCards() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        Card updatedCard = new Card();
        updatedCard.setId(1L);
        updatedCard.setMaskedNumber("**** ** ** 1234");
        updatedCard.setBalance(BigDecimal.valueOf(900));
        updatedCard.setStatus(CardStatus.ACTIVE);

        when(cardService.transferBetweenCards(any())).thenReturn(updatedCard);
        when(cardMapper.toDto(eq(updatedCard))).thenReturn(sampleCardDto);
    }
}
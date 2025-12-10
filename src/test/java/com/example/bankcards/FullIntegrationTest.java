//package com.example.bankcards;
//
//import com.example.bankcards.dto.UserDto;
//import com.example.bankcards.dto.TopUpRequest;
//import com.example.bankcards.dto.TransferRequest;
//import com.example.bankcards.entity.*;
//
//import com.example.bankcards.repository.CardRepository;
//import com.example.bankcards.repository.RoleRepository;
//import com.example.bankcards.repository.TransferRepository;
//import com.example.bankcards.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.checkerframework.checker.units.qual.C;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.*;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//@Testcontainers
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class FullIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
//            .withDatabaseName("bank_test")
//            .withUsername("test")
//            .withPassword("test");
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    @Autowired
//    private CardRepository cardRepository;
//
//    @Autowired
//    private TransferRepository transferRepository;
//
//    private static String accessToken;
//    private static Long userId;
//    private static Long card1;
//    private static Long card2;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private RoleRepository roleRepository;
//
//
//
//    @BeforeEach
//    void setupTestData() {
//        Role userRole = roleRepository.findByName("ADMIN")
//                .orElseGet(() -> {
//                    Role role = new Role();
//
//                    return roleRepository.save(role);
//                });
//
//        User testUser = new User();
//        testUser.setUsername("user05");
//        testUser.setPassword("pass123");
//        testUser.setRole(userRole);
//        testUser.setStatus(UserStatus.ACTIVE);
//        userRepository.save(testUser);
//
//        userId = testUser.getId();
//    }
//
//
//    @BeforeEach
//    void setupRestTemplate() {
//        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//    }
//
//
//    @Test
//    @Order(1)
//    void registerAndLogin() {
//        Map<String, String> registerBody = Map.of(
//                "username", "testuser11" +
//                        "",
//                "password", "pass123"
//        );
//
//        ResponseEntity<Map> registerRes = restTemplate.postForEntity("/api/register", registerBody, Map.class);
//        assertThat(registerRes.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//
//        ResponseEntity<Map> loginRes = restTemplate.postForEntity("/api/login", registerBody, Map.class);
//        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//        Map<String, Object> loginBody = loginRes.getBody();
//        accessToken = loginBody.get("access_token").toString();
//    }
//
//
//    @Test
//    @Order(2)
//    void issueTwoCards() {
//        card1 = createCard(userId);
//        card2 = createCard(userId);
//
//        assertThat(cardRepository.findAll()).hasSize(2);
//    }
//
//    @Test
//    @Order(3)
//    void topUpCard() {
//        HttpHeaders headers = bearer();
//        TopUpRequest req = new TopUpRequest(new BigDecimal("10000"));
//
//        ResponseEntity<String> res = restTemplate.exchange(
//                "/api/cards/" + card1 + "/top-up",
//                HttpMethod.PATCH,
//                new HttpEntity<>(req, headers),
//                String.class
//        );
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//    }
//
//    @Test
//    @Order(4)
//    void transferBetweenCards() {
//        HttpHeaders headers = bearer();
//        TransferRequest req = TransferRequest.builder()
//                .fromCardId(card1)
//                .toCardId(card2)
//                .amount(new BigDecimal("3000"))
//                .build();
//
//        ResponseEntity<String> res = restTemplate.exchange(
//                "/api/cards/transfer",
//                HttpMethod.POST,
//                new HttpEntity<>(req, headers),
//                String.class
//        );
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//    }
//
//    @Test
//    @Order(5)
//    void checkHistory() {
//        HttpHeaders headers = bearer();
//        ResponseEntity<String> res = restTemplate.exchange(
//                "/api/history?page=0&size=10",
//                HttpMethod.GET,
//                new HttpEntity<>(headers),
//                String.class
//        );
//
//        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//        long transfersCount = transferRepository.count();
//        assertThat(transfersCount).isEqualTo(1);
//    }
//
//
//    private Long createCard(Long userId) {
//        HttpHeaders headers = bearer();
//
//        ResponseEntity<Map> response = restTemplate.exchange(
//                "/api/create?userId=" + userId,
//                HttpMethod.POST,
//                new HttpEntity<>(null, headers),
//                Map.class
//        );
//
//        Map<String, Object> body = response.getBody();
//        if (body == null || body.get("id") == null) {
//            throw new IllegalStateException("Failed to create card: " + response);
//        }
//
//        return Long.valueOf(body.get("id").toString());
//    }
//
//    private HttpHeaders bearer() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        return headers;
//    }
//}

package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.LoginAttemptService;
import com.example.bankcards.service.AuditService;
import com.example.bankcards.service.AuthenticationService;
import com.google.common.util.concurrent.RateLimiter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    JwtUtil jwtUtil;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    private LoginAttemptService loginAttemptService;
    private AuditService auditService;



    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
//        String token = authenticationService.registerUser(userDto);
//        return ResponseEntity.ok(token); //сделала refresh и access токены это на всякий
        String access = authenticationService.registerUser(userDto);
        String refresh = jwtUtil.generateRefreshToken(userDto.getUsername());

        return ResponseEntity.ok(Map.of(
                "access_token", access,
                "refresh_token", refresh
        ));
    }


    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody UserDto dto) {
        String username = dto.getUsername();

        if (loginAttemptService.isBlocked(username)) {
            auditService.logLogin(username, false);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Слишком много попыток входа. Попробуйте позже.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("user not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(username);
            auditService.logLogin(username, false);
            throw new RuntimeException("wrong password");
        }

        loginAttemptService.loginSucceeded(username);
        auditService.logLogin(username, true);

        List<String> roles = List.of("ROLE_" + user.getRole().getName());
        return ResponseEntity.ok(Map.of(
                "access_token", jwtUtil.generateAccessToken(user.getUsername(), roles),
                "refresh_token", jwtUtil.generateRefreshToken(user.getUsername())
        ));
    }
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refresh = body.get("refresh_token");

        if (!jwtUtil.validateToken(refresh)) {
            return ResponseEntity.status(401).body("invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refresh);
        String newAccess = jwtUtil.generateAccessToken(username, jwtUtil.extractRoles(refresh));

        return ResponseEntity.ok(Map.of("access_token", newAccess));
    }
//    {
//           "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFzdFVzZXIiLCJpYXQiOjE3NjQ5NTgwMDMsImV4cCI6MTc2NDk1ODkwM30.Vr11-Zp8EA_2Kml57_mDYcJqEK_XVFSo2JOwG0vm2o4"
//            "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqdWFzdFVzZXIiLCJpYXQiOjE3NjQ5NTc4MjksImV4cCI6MTc2NzU0OTgyOX0.0NRKkYC19mTL9-JWrK33r69CGjRVsZo1_3EvLlrf5fs"
//    }


}

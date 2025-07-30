package com.example.bankcards.service;


import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository rolerepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String registerUser(UserDto user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("user with this name already exist");
        }

        Role roleUser;
        if (user.getRoleId() != null) {
            roleUser = rolerepository.findById(user.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Роль с id " + user.getRoleId() + " не найдена"));
        } else {
            roleUser = rolerepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Роль USER не найдена"));
        }

        User users = User.builder()
                .username(user.getUsername())
                .role(roleUser)
                .password(passwordEncoder.encode(user.getPassword()))
                .build();

        userRepository.saveAndFlush(users);

        List<String> roles = List.of("ROLE_" + roleUser.getName());
        return jwtUtil.generateToken(user.getUsername(), roles);
    }
}
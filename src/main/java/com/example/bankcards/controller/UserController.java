package com.example.bankcards.controller;

import com.example.bankcards.dto.AckDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserStatus;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserRepository userRepository;
    UserMapper userMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users")
    public List<UserDto> getAllUsers() {
        return userRepository.streamAllBy()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/delete-user/{user_id}")
    public AckDto deleteUser(@PathVariable("user_id") Long userId){

        User User = getUserOrThrowException(userId);
        userRepository.delete(User);
        return AckDto.builder().answer(true).build();


    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/block-user/{user_id}")
    public AckDto block(@PathVariable("user_id") Long userId){

        User user = getUserOrThrowException(userId);
        if (user.getStatus() == UserStatus.BLOCKED){
            throw new RuntimeException("user already blocked");
        }
        user.setStatus(UserStatus.BLOCKED);
        userRepository.saveAndFlush(user);
        return AckDto.builder().answer(true).build();


    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/unblock-user/{user_id}")
    public AckDto unblock(@PathVariable("user_id") Long userId){

        User user = getUserOrThrowException(userId);
        if (user.getStatus() == UserStatus.ACTIVE){
            throw new RuntimeException("card already active");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.saveAndFlush(user);
        return AckDto.builder().answer(true).build();


    }
    private User getUserOrThrowException(Long userId) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "User" +
                                                " with \"%s\" id doesn't exist.",
                                        userId
                                )
                        )
                );
    }
}

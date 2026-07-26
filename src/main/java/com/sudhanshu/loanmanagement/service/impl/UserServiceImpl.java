package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.UserRequestDto;
import com.sudhanshu.loanmanagement.dto.UserResponseDto;
import com.sudhanshu.loanmanagement.entity.Role;
import com.sudhanshu.loanmanagement.entity.User;
import com.sudhanshu.loanmanagement.exception.UserAlreadyExistsException;
import com.sudhanshu.loanmanagement.repository.UserRepository;
import com.sudhanshu.loanmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto registerUser(UserRequestDto dto) {

        log.info("Registering user : {}", dto.getUsername());

        if (userRepository.existsByUsername(dto.getUsername())) {

            log.error("Username already exists : {}", dto.getUsername());

            throw new UserAlreadyExistsException(
                    "Username already exists.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {

            log.error("Email already exists : {}", dto.getEmail());

            throw new UserAlreadyExistsException(
                    "Email already exists.");
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() != null ? dto.getRole() : Role.ROLE_CUSTOMER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully : {}", savedUser.getUsername());

        return mapToResponse(savedUser);
    }

    private UserResponseDto mapToResponse(User user) {

        return UserResponseDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .build();
    }
}
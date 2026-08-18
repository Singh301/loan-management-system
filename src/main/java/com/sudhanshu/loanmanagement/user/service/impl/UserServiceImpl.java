package com.sudhanshu.loanmanagement.user.service.impl;

import com.sudhanshu.loanmanagement.user.dto.UpdateUserRoleRequestDto;
import com.sudhanshu.loanmanagement.user.dto.UserRequestDto;
import com.sudhanshu.loanmanagement.user.dto.UserResponseDto;
import com.sudhanshu.loanmanagement.user.entity.Role;
import com.sudhanshu.loanmanagement.user.entity.User;
import com.sudhanshu.loanmanagement.exception.UserAlreadyExistsException;
import com.sudhanshu.loanmanagement.user.repository.UserRepository;
import com.sudhanshu.loanmanagement.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.sudhanshu.loanmanagement.customer.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.sudhanshu.loanmanagement.user.dto.UpdateUserRequestDto;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;

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

        customerRepository.findByEmail(savedUser.getEmail())
                .ifPresent(customer -> {

                    customer.setUser(savedUser);

                    customerRepository.save(customer);

                    log.info(
                            "Customer {} linked with user {}",
                            customer.getCustomerId(),
                            savedUser.getUsername()
                    );

                });

        log.info("User registered successfully : {}", savedUser.getUsername());

        return mapToResponse(savedUser);
    }

    @Override
    public UserResponseDto getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        log.info("Fetching current user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));

        return mapToResponse(user);
    }

    private UserResponseDto mapToResponse(User user) {

        return UserResponseDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .build();
    }

    @Override
    public Page<UserResponseDto> getAllUsers(
            int page,
            int size,
            String sortBy,
            String direction) {

        log.info(
                "Fetching users. page={}, size={}, sortBy={}, direction={}",
                page,
                size,
                sortBy,
                direction);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return userRepository
                .findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {

        log.info("Fetching user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {

                    log.error("User not found. ID={}", userId);

                    return new ResourceNotFoundException(
                            "User not found with ID : " + userId);
                });

        return mapToResponse(user);
    }

    @Override
    public UserResponseDto updateUser(
            Long userId,
            UpdateUserRequestDto dto) {

        log.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {

                    log.error("User not found. ID={}", userId);

                    return new ResourceNotFoundException(
                            "User not found with ID : " + userId);
                });

        userRepository.findByEmail(dto.getEmail())
                .ifPresent(existingUser -> {

                    if (!existingUser.getUserId().equals(userId)) {

                        throw new UserAlreadyExistsException(
                                "Email already exists.");
                    }

                });

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());

        User updatedUser = userRepository.save(user);

        log.info("User updated successfully. ID={}", userId);

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String loggedInUsername = authentication.getName();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found. ID={}", userId);
                    return new ResourceNotFoundException(
                            "User not found with ID : " + userId);
                });

        // Prevent self-deletion
        if (user.getUsername().equals(loggedInUsername)) {
            log.error("User tried to delete own account.");
            throw new IllegalArgumentException(
                    "You cannot delete your own account.");
        }

        // Already soft-deleted
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new IllegalStateException(
                    "User is already deleted.");
        }

        // Soft Delete
        user.setDeleted(true);
        user.setDeletedAt(java.time.LocalDateTime.now());
        user.setEnabled(false);   // also disable login

        userRepository.save(user);

        log.info("User soft-deleted successfully. ID={}", userId);
    }

    @Override
    public UserResponseDto enableDisableUser(
            Long userId,
            Boolean enabled) {

        log.info("Updating user status. ID={}, enabled={}", userId, enabled);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {

                    log.error("User not found. ID={}", userId);

                    return new ResourceNotFoundException(
                            "User not found with ID : " + userId);
                });

        user.setEnabled(enabled);

        User updatedUser = userRepository.save(user);

        log.info("User status updated successfully. ID={}", userId);

        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponseDto changeRole(
            Long userId,
            UpdateUserRoleRequestDto dto) {

        log.info("Changing role for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID : " + userId));

        try {

            Role role = Role.valueOf(dto.getRole());

            user.setRole(role);

        } catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException(
                    "Invalid role : " + dto.getRole());
        }

        User updatedUser = userRepository.save(user);

        log.info(
                "Role updated successfully for user {}",
                userId);

        return mapToResponse(updatedUser);
    }

}





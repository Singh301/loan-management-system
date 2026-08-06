package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.dto.UpdateUserRoleRequestDto;
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
import com.sudhanshu.loanmanagement.entity.Customer;
import com.sudhanshu.loanmanagement.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.sudhanshu.loanmanagement.dto.UpdateUserRequestDto;

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

        if (user.getUsername().equals(loggedInUsername)) {

            log.error("User tried to delete own account.");

            throw new IllegalArgumentException(
                    "You cannot delete your own account.");
        }

        if (!user.getEnabled()) {

            throw new IllegalStateException(
                    "User is already disabled.");
        }

        user.setEnabled(false);

        userRepository.save(user);

        log.info("User disabled successfully. ID={}", userId);

        // TODO
        // auditService.saveAudit(
        //      "USER",
        //      "DELETE",
        //      "User disabled. User ID: " + userId
        // );
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
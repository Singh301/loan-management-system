package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.UpdateUserRequestDto;
import com.sudhanshu.loanmanagement.dto.UpdateUserRoleRequestDto;
import com.sudhanshu.loanmanagement.dto.UserRequestDto;
import com.sudhanshu.loanmanagement.dto.UserResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponseDto registerUser(UserRequestDto dto);

    UserResponseDto getCurrentUser();

    Page<UserResponseDto> getAllUsers(
            int page,
            int size,
            String sortBy,
            String direction);

    UserResponseDto getUserById(Long userId);

    UserResponseDto updateUser(Long userId, UpdateUserRequestDto dto);

    void deleteUser(Long userId);

    UserResponseDto enableDisableUser(Long userId, Boolean enabled);

    UserResponseDto changeRole(Long userId, UpdateUserRoleRequestDto dto);
}
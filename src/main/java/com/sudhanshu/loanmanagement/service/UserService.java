package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.dto.UserRequestDto;
import com.sudhanshu.loanmanagement.dto.UserResponseDto;

public interface UserService {

    UserResponseDto registerUser(UserRequestDto dto);

}
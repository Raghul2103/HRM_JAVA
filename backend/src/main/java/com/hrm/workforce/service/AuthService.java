package com.hrm.workforce.service;

import com.hrm.workforce.dto.LoginRequest;
import com.hrm.workforce.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void register(String username, String password, String role);
}

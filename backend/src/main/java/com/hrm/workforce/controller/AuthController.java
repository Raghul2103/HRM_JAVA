package com.hrm.workforce.controller;

import com.hrm.workforce.dto.ApiResponse;
import com.hrm.workforce.dto.LoginRequest;
import com.hrm.workforce.dto.LoginResponse;
import com.hrm.workforce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login and operator registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new system operator user (Admin/HR/Supervisor/Payroll)")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestParam String username,
                                                        @Valid @RequestParam String password,
                                                        @Valid @RequestParam String role) {
        authService.register(username, password, role);
        return ResponseEntity.ok(ApiResponse.success("Operator registered successfully. Please login."));
    }
}

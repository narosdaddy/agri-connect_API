package com.cybernerd.agriConnect_APIBackend.service.authService;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.AuthResponse;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.LoginRequest;
import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;

public interface AuthService {
    AuthResponse registerUser(RegisterRequest request);

    AuthResponse loginUser(LoginRequest loginRequest);

    AuthResponse refreshToken(String token);

    void verifyEmail(String code);

    void sendVerificationEmail(String email, String code);

    void sendPasswordResetEmail(String email);

    boolean isEmailVerified(String email);

    void resendVerificationEmail(String email);
}

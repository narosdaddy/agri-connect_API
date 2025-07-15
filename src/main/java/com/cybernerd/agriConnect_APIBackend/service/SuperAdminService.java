package com.cybernerd.agriConnect_APIBackend.service;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
import com.cybernerd.agriConnect_APIBackend.enumType.Role;

public interface SuperAdminService {
    void createUser(RegisterRequest request, Role role);
} 
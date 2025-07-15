package com.cybernerd.agriConnect_APIBackend.controller;

import com.cybernerd.agriConnect_APIBackend.dtos.auth.RegisterRequest;
import com.cybernerd.agriConnect_APIBackend.enumType.Role;
import com.cybernerd.agriConnect_APIBackend.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {
    private final SuperAdminService superAdminService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody RegisterRequest request, @RequestParam Role role) {
        superAdminService.createUser(request, role);
        return ResponseEntity.ok("Utilisateur créé avec succès");
    }
} 
package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.dto.PermissionResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleResponseDTO;
import com.hvduong.detectiontomatoes.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class RoleManagementController {

    private final RoleService roleService;

    public RoleManagementController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponseDTO>> getAllPermissions() {
        return ResponseEntity.ok(roleService.getAllPermissions());
    }

    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleCreateDTO dto) {
        try {
            RoleResponseDTO savedRole = roleService.createRole(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

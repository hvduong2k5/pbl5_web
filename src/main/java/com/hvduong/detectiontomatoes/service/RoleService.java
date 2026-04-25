package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.dto.PermissionResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleResponseDTO;
import com.hvduong.detectiontomatoes.model.entity.Permission;
import com.hvduong.detectiontomatoes.model.entity.Role;
import com.hvduong.detectiontomatoes.repository.PermissionRepository;
import com.hvduong.detectiontomatoes.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionResponseDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionResponseDTO(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleResponseDTO createRole(RoleCreateDTO dto) {
        String upperName = dto.getName().toUpperCase();
        if (roleRepository.findByName(upperName).isPresent()) {
            throw new IllegalArgumentException("Vai trò đã tồn tại: " + upperName);
        }

        Role role = new Role();
        role.setName(upperName);

        Set<Permission> permissions = new HashSet<>();
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            List<Permission> foundPermissions = permissionRepository.findAllById(dto.getPermissionIds());
            if (foundPermissions.size() != dto.getPermissionIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều ID quyền không hợp lệ.");
            }
            permissions.addAll(foundPermissions);
        }
        role.setPermissions(permissions);

        Role savedRole = roleRepository.save(role);

        return RoleResponseDTO.builder()
                .id(savedRole.getId())
                .name(savedRole.getName())
                .permissions(savedRole.getPermissions().stream()
                        .map(p -> new PermissionResponseDTO(p.getId(), p.getName()))
                        .collect(Collectors.toList()))
                .build();
    }
}

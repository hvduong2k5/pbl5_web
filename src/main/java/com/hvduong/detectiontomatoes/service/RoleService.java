package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.dto.PermissionResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleUpdateDTO;
import com.hvduong.detectiontomatoes.model.entity.Permission;
import com.hvduong.detectiontomatoes.model.entity.Role;
import com.hvduong.detectiontomatoes.repository.PermissionRepository;
import com.hvduong.detectiontomatoes.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<RoleResponseDTO> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::mapToResponse);
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

        return mapToResponse(savedRole);
    }

    @Transactional
    public RoleResponseDTO updateRole(Integer id, RoleUpdateDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Role với ID: " + id));

        if (role.getName().equalsIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("Không thể sửa tên hoặc phân quyền của Role hệ thống ADMIN.");
        }

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            String upperName = dto.getName().trim().toUpperCase();
            if (!role.getName().equals(upperName) && roleRepository.findByName(upperName).isPresent()) {
                throw new IllegalArgumentException("Vai trò đã tồn tại: " + upperName);
            }
            role.setName(upperName);
        }

        if (dto.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>();
            if (!dto.getPermissionIds().isEmpty()) {
                List<Permission> foundPermissions = permissionRepository.findAllById(dto.getPermissionIds());
                if (foundPermissions.size() != dto.getPermissionIds().size()) {
                    throw new IllegalArgumentException("Một hoặc nhiều ID quyền không hợp lệ.");
                }
                permissions.addAll(foundPermissions);
            }
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        return mapToResponse(updatedRole);
    }

    @Transactional
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Role với ID: " + id));

        if (role.getName().equalsIgnoreCase("ADMIN") || role.getName().equalsIgnoreCase("OPERATOR")) {
            throw new IllegalArgumentException("Không thể xóa các Role hệ thống mặc định (ADMIN, OPERATOR).");
        }

        roleRepository.delete(role);
    }

    private RoleResponseDTO mapToResponse(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(role.getPermissions() != null ? role.getPermissions().stream()
                        .map(p -> new PermissionResponseDTO(p.getId(), p.getName()))
                        .collect(Collectors.toList()) : null)
                .build();
    }
}

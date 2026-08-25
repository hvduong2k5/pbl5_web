package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.dto.PermissionResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleUpdateDTO;
import com.hvduong.detectiontomatoes.model.entity.Permission;
import com.hvduong.detectiontomatoes.model.entity.Role;
import com.hvduong.detectiontomatoes.repository.PermissionRepository;
import com.hvduong.detectiontomatoes.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    private Role mockRole;
    private Permission mockPermission;

    @BeforeEach
    void setUp() {
        mockPermission = new Permission();
        mockPermission.setId(1);
        mockPermission.setName("READ_TEST");

        mockRole = new Role();
        mockRole.setId(1);
        mockRole.setName("USER");
        mockRole.setPermissions(Set.of(mockPermission));
    }

    @Test
    void testGetAllPermissions() {
        when(permissionRepository.findAll()).thenReturn(Collections.singletonList(mockPermission));

        List<PermissionResponseDTO> result = roleService.getAllPermissions();

        assertEquals(1, result.size());
        assertEquals("READ_TEST", result.get(0).getName());
    }

    @Test
    void testGetAllRoles() {
        when(roleRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockRole)));

        Page<RoleResponseDTO> result = roleService.getAllRoles(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("USER", result.getContent().get(0).getName());
    }

    @Test
    void testCreateRole_Success() {
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setName("manager");
        dto.setPermissionIds(List.of(1));

        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());
        when(permissionRepository.findAllById(dto.getPermissionIds())).thenReturn(Collections.singletonList(mockPermission));
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> {
            Role r = i.getArgument(0);
            r.setId(2);
            return r;
        });

        RoleResponseDTO response = roleService.createRole(dto);

        assertNotNull(response);
        assertEquals("MANAGER", response.getName());
        assertEquals(1, response.getPermissions().size());
    }

    @Test
    void testCreateRole_AlreadyExists() {
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setName("USER");

        when(roleRepository.findByName("USER")).thenReturn(Optional.of(mockRole));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roleService.createRole(dto);
        });
        assertEquals("Vai trò đã tồn tại: USER", exception.getMessage());
    }

    @Test
    void testUpdateRole_AdminLockout() {
        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");

        RoleUpdateDTO dto = new RoleUpdateDTO();
        dto.setName("SUPERADMIN");

        when(roleRepository.findById(1)).thenReturn(Optional.of(adminRole));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roleService.updateRole(1, dto);
        });
        assertEquals("Không thể sửa tên hoặc phân quyền của Role hệ thống ADMIN.", exception.getMessage());
    }

    @Test
    void testDeleteRole_AdminLockout() {
        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setName("ADMIN");

        when(roleRepository.findById(1)).thenReturn(Optional.of(adminRole));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roleService.deleteRole(1);
        });
        assertEquals("Không thể xóa các Role hệ thống mặc định (ADMIN, OPERATOR).", exception.getMessage());
    }

    @Test
    void testDeleteRole_Success() {
        when(roleRepository.findById(1)).thenReturn(Optional.of(mockRole));

        roleService.deleteRole(1);

        verify(roleRepository, times(1)).delete(mockRole);
    }
}

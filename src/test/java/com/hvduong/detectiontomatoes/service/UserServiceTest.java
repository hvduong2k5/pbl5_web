package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.dto.UserCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.UserResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.UserRolesUpdateDTO;
import com.hvduong.detectiontomatoes.model.dto.UserUpdateDTO;
import com.hvduong.detectiontomatoes.model.entity.Role;
import com.hvduong.detectiontomatoes.model.entity.User;
import com.hvduong.detectiontomatoes.repository.RoleRepository;
import com.hvduong.detectiontomatoes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private Role mockAdminRole;

    @BeforeEach
    void setUp() {
        mockAdminRole = new Role();
        mockAdminRole.setName("ADMIN");

        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setEnabled(true);
        mockUser.setRoles(Set.of(mockAdminRole));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockUser)));

        Page<UserResponseDTO> result = userService.getAllUsers(null, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    @Test
    void testCreateUser_Success() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("newuser");
        dto.setPassword("pwd");
        dto.setRoles(Set.of("ADMIN"));

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pwd")).thenReturn("encodedPwd");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(mockAdminRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2);
            return u;
        });

        UserResponseDTO response = userService.createUser(dto);

        assertNotNull(response);
        assertEquals(2, response.getId());
        assertEquals("newuser", response.getUsername());
        assertTrue(response.getRoles().contains("ADMIN"));
    }

    @Test
    void testCreateUser_AlreadyExists() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(dto);
        });
        assertEquals("Tài khoản đã tồn tại trong hệ thống.", exception.getMessage());
    }

    @Test
    void testUpdateUser_Success() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEnabled(false);

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Mock Cache eviction
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("userDetails")).thenReturn(mockCache);

        UserResponseDTO response = userService.updateUser(1, dto, "anotheradmin");

        assertFalse(response.getEnabled());
        verify(mockCache, times(1)).evict("testuser");
    }

    @Test
    void testUpdateUser_SelfDisableLockout() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEnabled(false);

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(1, dto, "testuser");
        });
        assertEquals("Bạn không thể tự khóa (disable) tài khoản của chính mình.", exception.getMessage());
    }

    @Test
    void testUpdateUserRoles_SelfAdminRemovalLockout() {
        UserRolesUpdateDTO dto = new UserRolesUpdateDTO();
        dto.setRoles(Set.of("OPERATOR")); // Removing ADMIN

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRoles(1, dto, "testuser");
        });
        assertEquals("Bạn không thể tự xóa quyền ADMIN của chính mình.", exception.getMessage());
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("userDetails")).thenReturn(mockCache);

        userService.deleteUser(1, "anotheradmin");

        verify(userRepository, times(1)).deleteById(1);
        verify(mockCache, times(1)).evict("testuser");
    }

    @Test
    void testDeleteUser_SelfDeleteLockout() {
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1, "testuser");
        });
        assertEquals("Bạn không thể tự xóa tài khoản của chính mình.", exception.getMessage());
        verify(userRepository, never()).deleteById(1);
    }
}

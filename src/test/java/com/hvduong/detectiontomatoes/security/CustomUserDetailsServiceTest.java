package com.hvduong.detectiontomatoes.security;

import com.hvduong.detectiontomatoes.model.entity.Permission;
import com.hvduong.detectiontomatoes.model.entity.Role;
import com.hvduong.detectiontomatoes.model.entity.User;
import com.hvduong.detectiontomatoes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        Permission readPermission = new Permission();
        readPermission.setName("READ_PRIVILEGE");

        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setPermissions(Set.of(readPermission));

        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setEnabled(true);
        mockUser.setRoles(Set.of(userRole));
    }

    @Test
    void testLoadUserByUsername_Success() {
        Mockito.when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        
        // Authorities should contain ROLE_USER and READ_PRIVILEGE
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("READ_PRIVILEGE")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });
    }
}

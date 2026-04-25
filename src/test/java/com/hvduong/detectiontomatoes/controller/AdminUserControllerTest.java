package com.hvduong.detectiontomatoes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hvduong.detectiontomatoes.model.dto.UserCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.UserResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.UserUpdateDTO;
import com.hvduong.detectiontomatoes.security.CustomUserDetailsService;
import com.hvduong.detectiontomatoes.security.JwtTokenProvider;
import com.hvduong.detectiontomatoes.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to focus on controller logic
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponseDTO mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUserResponse = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(Set.of("OPERATOR"))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(Collections.singletonList(mockUserResponse));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].roles[0]").value("OPERATOR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser_Success() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setEnabled(true);
        dto.setRoles(Set.of("OPERATOR"));

        UserResponseDTO savedUserResponse = UserResponseDTO.builder()
                .id(2)
                .username("newuser")
                .enabled(true)
                .roles(Set.of("OPERATOR"))
                .build();

        Mockito.when(userService.createUser(any(UserCreateDTO.class))).thenReturn(savedUserResponse);

        mockMvc.perform(post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser_Conflict() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser"); // Already exists
        dto.setPassword("123456");

        Mockito.when(userService.createUser(any(UserCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Tài khoản đã tồn tại trong hệ thống."));

        mockMvc.perform(post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_Success() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEnabled(false);

        UserResponseDTO updatedUserResponse = UserResponseDTO.builder()
                .id(1)
                .username("testuser")
                .enabled(false)
                .roles(Set.of("OPERATOR"))
                .build();

        Mockito.when(userService.updateUser(eq(1), any(UserUpdateDTO.class))).thenReturn(updatedUserResponse);

        mockMvc.perform(put("/api/admin/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/api/admin/users/1").with(csrf()))
                .andExpect(status().isOk());
    }
}

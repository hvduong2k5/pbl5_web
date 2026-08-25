package com.hvduong.detectiontomatoes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hvduong.detectiontomatoes.model.dto.PermissionResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.RoleUpdateDTO;
import com.hvduong.detectiontomatoes.security.CustomUserDetailsService;
import com.hvduong.detectiontomatoes.security.JwtTokenProvider;
import com.hvduong.detectiontomatoes.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RoleManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class RoleManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllPermissions() throws Exception {
        PermissionResponseDTO dto = new PermissionResponseDTO(1, "READ_TEST");
        Mockito.when(roleService.getAllPermissions()).thenReturn(Collections.singletonList(dto));

        mockMvc.perform(get("/api/admin/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("READ_TEST"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllRoles() throws Exception {
        RoleResponseDTO dto = RoleResponseDTO.builder().id(1).name("USER").build(); 
        Mockito.when(roleService.getAllRoles(any())).thenReturn(new PageImpl<>(Collections.singletonList(dto)));

        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateRole_Success() throws Exception {
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setName("manager");
        dto.setPermissionIds(List.of(1));

        RoleResponseDTO responseDTO = RoleResponseDTO.builder().id(2).name("MANAGER").build();
        Mockito.when(roleService.createRole(any(RoleCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/admin/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("MANAGER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateRole_Conflict() throws Exception {
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setName("manager");
        
        Mockito.when(roleService.createRole(any(RoleCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Role already exists"));

        mockMvc.perform(post("/api/admin/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateRole_Success() throws Exception {
        RoleUpdateDTO dto = new RoleUpdateDTO();
        dto.setName("superadmin");

        RoleResponseDTO responseDTO = RoleResponseDTO.builder().id(1).name("SUPERADMIN").build();
        Mockito.when(roleService.updateRole(eq(1), any(RoleUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/admin/roles/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SUPERADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteRole_Success() throws Exception {
        Mockito.doNothing().when(roleService).deleteRole(1);

        mockMvc.perform(delete("/api/admin/roles/1").with(csrf()))
                .andExpect(status().isOk());
    }
}

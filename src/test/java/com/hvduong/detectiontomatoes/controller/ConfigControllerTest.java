package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.security.CustomUserDetailsService;
import com.hvduong.detectiontomatoes.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemConfigRepository systemConfigRepository;

    @MockBean
    private BatchRepository batchRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser
    void testGetCurrentBatch_Exists() throws Exception {
        Batch batch = new Batch();
        batch.setId(1);
        batch.setName("Test Batch");

        SystemConfig config = new SystemConfig();
        config.setId(1);
        config.setCurrentBatch(batch);

        Mockito.when(systemConfigRepository.findById(1)).thenReturn(Optional.of(config));

        mockMvc.perform(get("/api/config/current-batch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Batch"));
    }

    @Test
    @WithMockUser
    void testGetCurrentBatch_NotFound() throws Exception {
        Mockito.when(systemConfigRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/config/current-batch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @WithMockUser
    void testGetBatchList() throws Exception {
        Batch batch = new Batch();
        batch.setId(1);
        batch.setName("Test Batch");

        Mockito.when(batchRepository.findAll()).thenReturn(Collections.singletonList(batch));

        mockMvc.perform(get("/api/config/batch/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Batch"));
    }
}

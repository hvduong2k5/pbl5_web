package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.security.CustomUserDetailsService;
import com.hvduong.detectiontomatoes.security.JwtTokenProvider;
import com.hvduong.detectiontomatoes.service.BatchService;
import com.hvduong.detectiontomatoes.service.FruitExportService;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BatchManageController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class BatchManageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BatchService batchService;

    @MockBean
    private FruitExportService fruitExportService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(authorities = "MANAGE_BATCH")
    void testCreateNewBatch() throws Exception {
        Map<String, Object> batchMap = new HashMap<>();
        batchMap.put("id", 1);
        batchMap.put("name", "New Batch");

        Mockito.when(batchService.createNewBatch("New Batch")).thenReturn(batchMap);

        mockMvc.perform(post("/api/batch/new")
                        .param("name", "New Batch")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Batch"));
    }

    @Test
    @WithMockUser(username = "user")
    void testGetCurrentBatch_Exists() throws Exception {
        Map<String, Object> batchMap = new HashMap<>();
        batchMap.put("id", 1);

        Mockito.when(batchService.getCurrentBatch()).thenReturn(batchMap);

        mockMvc.perform(get("/api/batch/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "user")
    void testGetCurrentBatch_NotFound() throws Exception {
        Mockito.when(batchService.getCurrentBatch()).thenReturn(null);

        mockMvc.perform(get("/api/batch/current"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "EXPORT_DATA")
    void testExportCurrentBatchToExcel_Success() throws Exception {
        Mockito.when(batchService.getCurrentBatchId()).thenReturn(100);
        Mockito.doNothing().when(fruitExportService).exportFruitsByBatch(eq(100), any());

        mockMvc.perform(get("/api/batch/current/export"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "EXPORT_DATA")
    void testExportCurrentBatchToExcel_NoBatch() throws Exception {
        Mockito.when(batchService.getCurrentBatchId()).thenReturn(null);

        mockMvc.perform(get("/api/batch/current/export"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "VIEW_HISTORY")
    void testGetAllBatches() throws Exception {
        Map<String, Object> batchMap = new HashMap<>();
        batchMap.put("id", 1);
        
        Mockito.when(batchService.getAllBatches()).thenReturn(Collections.singletonList(batchMap));

        mockMvc.perform(get("/api/batch/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}

package com.hvduong.detectiontomatoes.controller;

import com.hvduong.detectiontomatoes.security.CustomUserDetailsService;
import com.hvduong.detectiontomatoes.security.JwtTokenProvider;
import com.hvduong.detectiontomatoes.service.FruitExportService;
import com.hvduong.detectiontomatoes.service.FruitService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FruitService fruitService;

    @MockBean
    private FruitExportService fruitExportService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user")
    void testGetFruitsByBatch() throws Exception {
        Map<String, Object> fruitMap = new HashMap<>();
        fruitMap.put("id", 1);
        fruitMap.put("label", "ripe");
        
        Mockito.when(fruitService.getFruitsByBatch(100)).thenReturn(Collections.singletonList(fruitMap));

        mockMvc.perform(get("/api/batch/100/fruits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("ripe"));
    }

    @Test
    @WithMockUser(authorities = "EXPORT_DATA")
    void testExportToExcel_Success() throws Exception {
        Mockito.doNothing().when(fruitExportService).exportFruitsByBatch(eq(100), any());

        mockMvc.perform(get("/api/batch/100/export"))
                .andExpect(status().isOk());
    }
}

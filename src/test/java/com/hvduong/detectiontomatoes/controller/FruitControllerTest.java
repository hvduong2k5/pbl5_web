package com.hvduong.detectiontomatoes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hvduong.detectiontomatoes.model.dto.AiResponseDTO;
import com.hvduong.detectiontomatoes.security.CustomUserDetailsService;
import com.hvduong.detectiontomatoes.security.JwtTokenProvider;
import com.hvduong.detectiontomatoes.service.FruitService;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(FruitController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class FruitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FruitService fruitService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void testAiResponse() throws Exception {
        AiResponseDTO dto = new AiResponseDTO();
        dto.setId("esp_1");
        dto.setResult("ripe");
        dto.setConfidence(0.95);

        Mockito.doNothing().when(fruitService).handleAiResponse(any(AiResponseDTO.class));

        mockMvc.perform(post("/api/fruit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "VIEW_HISTORY")
    void testGetAllFruits() throws Exception {
        Map<String, Object> fruitMap = new HashMap<>();
        fruitMap.put("id", 1);
        fruitMap.put("label", "ripe");

        Mockito.when(fruitService.getAllFruits()).thenReturn(Collections.singletonList(fruitMap));

        mockMvc.perform(get("/api/fruit/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("ripe"));
    }

    @Test
    @WithMockUser(username = "user")
    void testGetStats() throws Exception {
        Map<String, Integer> statsMap = new HashMap<>();
        statsMap.put("total", 100);

        Mockito.when(fruitService.getCurrentStats()).thenReturn(statsMap);

        mockMvc.perform(get("/api/fruit/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100));
    }
}

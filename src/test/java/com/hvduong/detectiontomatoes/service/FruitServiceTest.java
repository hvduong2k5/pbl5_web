package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.dto.AiResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.FruitEventDTO;
import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.model.mapper.FruitMapper;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.websocket.WebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FruitServiceTest {

    @Mock
    private FruitRepository fruitRepository;

    @Mock
    private FruitMapper fruitMapper;

    @Mock
    private WebSocketHandler webSocketHandler;

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @InjectMocks
    private FruitService fruitService;

    private Batch currentBatch;
    private SystemConfig systemConfig;

    @BeforeEach
    void setUp() {
        currentBatch = new Batch();
        currentBatch.setId(1);
        currentBatch.setName("Test Batch");

        systemConfig = new SystemConfig();
        systemConfig.setId(1);
        systemConfig.setCurrentBatch(currentBatch);
    }

    @Test
    void testGetCurrentStats_WithActiveBatch() {
        // Arrange
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(systemConfig));
        when(fruitRepository.countByBatch_Id(1)).thenReturn(10L);
        when(fruitRepository.countByBatch_IdAndStatus(1, "DETECTED")).thenReturn(2L);

        List<Object[]> labelCounts = new ArrayList<>();
        labelCounts.add(new Object[]{"ripe", 5L});
        labelCounts.add(new Object[]{"green", 2L});
        labelCounts.add(new Object[]{"reject", 1L});
        
        when(fruitRepository.countLabelsByBatchId(1)).thenReturn(labelCounts);

        // Act
        Map<String, Integer> stats = fruitService.getCurrentStats();

        // Assert
        assertEquals(10, stats.get("total"));
        assertEquals(2, stats.get("wait"));
        assertEquals(5, stats.get("ripe"));
        assertEquals(2, stats.get("unripe")); // "green" maps to "unripe"
        assertEquals(1, stats.get("reject"));
    }

    @Test
    void testHandleDetected_NewFruit() {
        // Arrange
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(systemConfig));
        
        FruitEventDTO dto = new FruitEventDTO();
        dto.setId("esp_123");
        dto.setConfidence(0.95);
        dto.setImageUrl("test.jpg");

        when(fruitRepository.findByEspIdAndBatch_Id("esp_123", 1)).thenReturn(Optional.empty());
        
        Fruit mappedFruit = new Fruit();
        mappedFruit.setEspId("esp_123");
        when(fruitMapper.toEntity(dto)).thenReturn(mappedFruit);
        
        // Mock getStatsByBatch inside getCurrentStats
        when(fruitRepository.countByBatch_Id(1)).thenReturn(1L);
        when(fruitRepository.countByBatch_IdAndStatus(1, "DETECTED")).thenReturn(1L);

        // Act
        fruitService.handleDetected(dto);

        // Assert
        ArgumentCaptor<Fruit> fruitCaptor = ArgumentCaptor.forClass(Fruit.class);
        verify(fruitRepository, times(1)).save(fruitCaptor.capture());
        
        Fruit savedFruit = fruitCaptor.getValue();
        assertEquals("DETECTED", savedFruit.getStatus());
        assertEquals(0.95, savedFruit.getConfidence());
        assertEquals("test.jpg", savedFruit.getImageUrl());
        assertEquals(currentBatch, savedFruit.getBatch());
        
        verify(webSocketHandler, times(1)).broadcastEvent(any());
        verify(webSocketHandler, times(1)).broadcastStats(anyMap());
    }

    @Test
    void testHandleDetected_DuplicateEspIdInSameBatch() {
        // Arrange
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(systemConfig));
        
        FruitEventDTO dto = new FruitEventDTO();
        dto.setId("esp_dup");
        
        Fruit existingFruit = new Fruit();
        existingFruit.setEspId("esp_dup");
        existingFruit.setStatus("SORTED");
        existingFruit.setLabel("ripe");
        existingFruit.setBatch(currentBatch);
        
        // Found existing fruit
        when(fruitRepository.findByEspIdAndBatch_Id("esp_dup", 1)).thenReturn(Optional.of(existingFruit));

        // Mock getStatsByBatch inside getCurrentStats
        when(fruitRepository.countByBatch_Id(1)).thenReturn(1L);
        when(fruitRepository.countByBatch_IdAndStatus(1, "DETECTED")).thenReturn(1L);

        // Act
        fruitService.handleDetected(dto);

        // Assert
        ArgumentCaptor<Fruit> fruitCaptor = ArgumentCaptor.forClass(Fruit.class);
        verify(fruitRepository, times(1)).save(fruitCaptor.capture());
        
        Fruit savedFruit = fruitCaptor.getValue();
        // It should reset the status to DETECTED (and keep ESP_ID)
        assertEquals("DETECTED", savedFruit.getStatus());
        
        verify(webSocketHandler, times(1)).broadcastEvent(any());
        verify(webSocketHandler, times(1)).broadcastStats(anyMap());
    }

    @Test
    void testHandleClassified() {
        // Arrange
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(systemConfig));
        
        FruitEventDTO dto = new FruitEventDTO();
        dto.setId("esp_123");
        dto.setLabel("ripe");
        dto.setConfidence(0.8);
        
        Fruit existingFruit = new Fruit();
        existingFruit.setEspId("esp_123");
        existingFruit.setStatus("DETECTED");
        existingFruit.setBatch(currentBatch);
        
        when(fruitRepository.findByEspIdAndBatch_Id("esp_123", 1)).thenReturn(Optional.of(existingFruit));

        // Act
        fruitService.handleClassified(dto);

        // Assert
        ArgumentCaptor<Fruit> fruitCaptor = ArgumentCaptor.forClass(Fruit.class);
        verify(fruitRepository, times(1)).save(fruitCaptor.capture());
        
        Fruit savedFruit = fruitCaptor.getValue();
        assertEquals("CLASSIFIED", savedFruit.getStatus());
        assertEquals("ripe", savedFruit.getLabel());
        assertEquals(0.8, savedFruit.getConfidence());
        assertNotNull(savedFruit.getClassifiedAt());
    }

    @Test
    void testHandleAiResponse_ExistingFruit() {
        // Arrange
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(systemConfig));
        
        AiResponseDTO dto = new AiResponseDTO();
        dto.setId("esp_123");
        dto.setResult("unripe");
        dto.setConfidence(0.99);
        
        Fruit existingFruit = new Fruit();
        existingFruit.setEspId("esp_123");
        existingFruit.setStatus("DETECTED");
        existingFruit.setBatch(currentBatch);
        
        when(fruitRepository.findByEspIdAndBatch_Id("esp_123", 1)).thenReturn(Optional.of(existingFruit));

        // Act
        fruitService.handleAiResponse(dto);

        // Assert
        verify(fruitMapper, times(1)).updateFromAiResponse(existingFruit, dto);
        ArgumentCaptor<Fruit> fruitCaptor = ArgumentCaptor.forClass(Fruit.class);
        verify(fruitRepository, times(1)).save(fruitCaptor.capture());
        
        Fruit savedFruit = fruitCaptor.getValue();
        assertEquals("CLASSIFIED", savedFruit.getStatus());
        assertNotNull(savedFruit.getClassifiedAt());
    }

    @Test
    void testHandleAiResponse_NewFruit() {
        // Arrange
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(systemConfig));
        
        AiResponseDTO dto = new AiResponseDTO();
        dto.setId("esp_new");
        dto.setResult("ripe");
        dto.setImageUrl("test_ai.jpg");
        dto.setConfidence(0.9);
        
        when(fruitRepository.findByEspIdAndBatch_Id("esp_new", 1)).thenReturn(Optional.empty());

        // Act
        fruitService.handleAiResponse(dto);

        // Assert
        ArgumentCaptor<Fruit> fruitCaptor = ArgumentCaptor.forClass(Fruit.class);
        verify(fruitRepository, times(1)).save(fruitCaptor.capture());
        
        Fruit savedFruit = fruitCaptor.getValue();
        assertEquals("CLASSIFIED", savedFruit.getStatus());
        assertEquals("esp_new", savedFruit.getEspId());
        assertEquals("ripe", savedFruit.getLabel());
        assertEquals("test_ai.jpg", savedFruit.getImageUrl());
        assertEquals(0.9, savedFruit.getConfidence());
        assertEquals(currentBatch, savedFruit.getBatch());
    }
}

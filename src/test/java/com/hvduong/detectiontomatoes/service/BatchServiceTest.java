package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.entity.Batch;
import com.hvduong.detectiontomatoes.model.entity.SystemConfig;
import com.hvduong.detectiontomatoes.repository.BatchRepository;
import com.hvduong.detectiontomatoes.repository.SystemConfigRepository;
import com.hvduong.detectiontomatoes.websocket.WebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private WebSocketHandler webSocketHandler;

    @InjectMocks
    private BatchService batchService;

    private Batch mockBatch;
    private SystemConfig mockConfig;

    @BeforeEach
    void setUp() {
        mockBatch = new Batch();
        mockBatch.setId(100);
        mockBatch.setName("Test Batch");
        mockBatch.setCreatedAt(LocalDateTime.now());

        mockConfig = new SystemConfig();
        mockConfig.setId(1);
        mockConfig.setCurrentBatch(mockBatch);
    }

    @Test
    void testToBatchMap() {
        Map<String, Object> map = batchService.toBatchMap(mockBatch);
        assertEquals(100, map.get("id"));
        assertEquals("Test Batch", map.get("name"));
        assertNotNull(map.get("createdAt"));
    }

    @Test
    void testToBatchMap_Null() {
        Map<String, Object> map = batchService.toBatchMap(null);
        assertTrue(map.isEmpty());
    }

    @Test
    void testCreateNewBatch() {
        when(batchRepository.save(any(Batch.class))).thenAnswer(i -> {
            Batch b = i.getArgument(0);
            b.setId(101);
            return b;
        });
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(mockConfig));

        Map<String, Object> result = batchService.createNewBatch("New Batch");

        assertNotNull(result);
        assertEquals(101, result.get("id"));
        assertEquals("New Batch", result.get("name"));
        
        verify(systemConfigRepository, times(1)).save(mockConfig);
        assertEquals(101, mockConfig.getCurrentBatch().getId());
        verify(webSocketHandler, times(1)).broadcastStats(anyMap());
    }

    @Test
    void testGetCurrentBatch() {
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(mockConfig));

        Map<String, Object> result = batchService.getCurrentBatch();

        assertNotNull(result);
        assertEquals(100, result.get("id"));
    }

    @Test
    void testGetCurrentBatch_NoConfig() {
        when(systemConfigRepository.findById(1)).thenReturn(Optional.empty());

        Map<String, Object> result = batchService.getCurrentBatch();

        assertNull(result);
    }

    @Test
    void testGetCurrentBatchId() {
        when(systemConfigRepository.findById(1)).thenReturn(Optional.of(mockConfig));

        Integer id = batchService.getCurrentBatchId();

        assertEquals(100, id);
    }

    @Test
    void testGetAllBatches() {
        when(batchRepository.findAll()).thenReturn(Collections.singletonList(mockBatch));

        List<Map<String, Object>> results = batchService.getAllBatches();

        assertEquals(1, results.size());
        assertEquals(100, results.get(0).get("id"));
    }
}

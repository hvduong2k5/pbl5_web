package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.entity.Fruit;
import com.hvduong.detectiontomatoes.repository.FruitRepository;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FruitExportServiceTest {

    @Mock
    private FruitRepository fruitRepository;

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private FruitExportService fruitExportService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fruitExportService, "bucketName", "test-bucket");
    }

    @Test
    void testExportFruitsByBatch_Success() throws Exception {
        Fruit fruit = new Fruit();
        fruit.setId(1);
        fruit.setEspId("esp-1");
        fruit.setLabel("ripe");
        fruit.setImageUrl("http://localhost:9000/test-bucket/test.jpg");

        when(fruitRepository.findAllByBatch_Id(eq(100), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(fruit)));

        // Mock MinioClient returning dummy response
        GetObjectResponse mockResponse = Mockito.mock(GetObjectResponse.class);
        when(mockResponse.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1); // Return EOF immediately

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        MockHttpServletResponse response = new MockHttpServletResponse();

        fruitExportService.exportFruitsByBatch(100, response);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8", response.getContentType());
        assertTrue(response.getHeader("Content-disposition").contains("Batch_100_Export.xlsx"));
        
        // Since EasyExcel writes to the output stream, it should not be empty
        assertTrue(response.getContentAsByteArray().length > 0);
        
        verify(fruitRepository, times(1)).findAllByBatch_Id(eq(100), any(Pageable.class));
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }

    @Test
    void testExportFruitsByBatch_MinioError() throws Exception {
        Fruit fruit = new Fruit();
        fruit.setId(2);
        fruit.setEspId("esp-2");
        fruit.setImageUrl("http://localhost:9000/test-bucket/error.jpg");

        when(fruitRepository.findAllByBatch_Id(eq(101), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(fruit)));

        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("MinIO is down"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Should not throw exception, just log and continue
        fruitExportService.exportFruitsByBatch(101, response);

        assertTrue(response.getContentAsByteArray().length > 0);
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }
}

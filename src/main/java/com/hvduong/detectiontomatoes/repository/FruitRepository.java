package com.hvduong.detectiontomatoes.repository;

import com.hvduong.detectiontomatoes.model.entity.Fruit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FruitRepository extends JpaRepository<Fruit, String> {
    List<Fruit> findAllByBatch_Id(Integer batchId);
    
    Page<Fruit> findAllByBatch_Id(Integer batchId, Pageable pageable);
    
    long countByBatch_Id(Integer batchId);
    
    long countByBatch_IdAndStatus(Integer batchId, String status);
    
    long countByBatch_IdAndStatusIn(Integer batchId, List<String> statuses);
    
    @Query("SELECT LOWER(f.label), COUNT(f) FROM Fruit f WHERE f.batch.id = :batchId AND f.label IS NOT NULL GROUP BY LOWER(f.label)")
    List<Object[]> countLabelsByBatchId(@Param("batchId") Integer batchId);
}

package com.hvduong.detectiontomatoes.repository;

import com.hvduong.detectiontomatoes.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
}

package com.hvduong.detectiontomatoes.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UserRolesUpdateDTO {
    @NotEmpty(message = "Danh sách vai trò (roles) không được để trống")
    private Set<String> roles;
}

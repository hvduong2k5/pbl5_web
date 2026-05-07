package com.hvduong.detectiontomatoes.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class RoleUpdateDTO {
    @Size(min = 2, max = 50, message = "Tên vai trò phải từ 2 đến 50 ký tự")
    private String name;

    private List<Integer> permissionIds;
}

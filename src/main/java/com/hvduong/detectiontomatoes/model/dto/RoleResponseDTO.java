package com.hvduong.detectiontomatoes.model.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RoleResponseDTO {
    private Integer id;
    private String name;
    private List<PermissionResponseDTO> permissions;
}

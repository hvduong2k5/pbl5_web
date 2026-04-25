package com.hvduong.detectiontomatoes.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateDTO {
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Boolean enabled;

    private Set<String> roles;
}

package com.hvduong.detectiontomatoes.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlCommandDTO {
    private String command; // START / STOP
}


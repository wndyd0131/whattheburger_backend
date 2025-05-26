package com.whattheburger.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CategoryReadResponseDto {
    private Long categoryId;
    private String name;
    private Integer productCount;
}

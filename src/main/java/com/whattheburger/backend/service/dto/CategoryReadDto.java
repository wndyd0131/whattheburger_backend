package com.whattheburger.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CategoryReadDto {
    private Long categoryId;
    private String categoryName;
    private int productCount;
}

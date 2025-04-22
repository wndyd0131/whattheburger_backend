package com.whataburger.whataburgerproject.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CategoryReadResponseDto {
    private Long categoryId;
    private String name;
    private Integer productCount;
}

package com.whataburger.whataburgerproject.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OptionReadResponseDto {
    private Long optionId;
    private String optionName;
    private String imageSource;
    private Double optionCalories;
}

package com.whattheburger.backend.controller.dto.option;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OptionReadResponseDto {
    private Long optionId;
    private String optionName;
    private String imageSource;
    private Double optionCalories;
    private List<OptionQuantityDto> quantityDetails;
}

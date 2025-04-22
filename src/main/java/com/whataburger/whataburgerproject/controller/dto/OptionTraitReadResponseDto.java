package com.whataburger.whataburgerproject.controller.dto;

import com.whataburger.whataburgerproject.domain.enums.OptionTraitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionTraitReadResponseDto {
    private Long optionTraitId;
    private String name;
    private String labelCode;
    private OptionTraitType optionTraitType;
}

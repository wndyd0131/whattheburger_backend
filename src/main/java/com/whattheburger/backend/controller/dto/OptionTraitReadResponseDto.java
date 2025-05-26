package com.whattheburger.backend.controller.dto;

import com.whattheburger.backend.domain.enums.OptionTraitType;
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

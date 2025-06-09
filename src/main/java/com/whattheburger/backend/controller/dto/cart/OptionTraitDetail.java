package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionTraitDetail {
    private Long productOptionTraitId;
    private Integer currentValue;

//    private Integer defaultValue;
//    private Double extraCalories;
//    private Double extraPrice;
//    private String labelCode;
//    private String name;
//    private OptionTraitType optionTraitType;
}

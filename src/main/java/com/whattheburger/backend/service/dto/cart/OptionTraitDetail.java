package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.ProductOptionTrait;
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
    private String labelCode;
    private String optionTraitName;
    private OptionTraitType optionTraitType;
}

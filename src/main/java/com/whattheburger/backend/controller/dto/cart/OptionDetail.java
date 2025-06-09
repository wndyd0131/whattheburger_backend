package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.MeasureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionDetail {
    private Long productOptionId;
    private Integer optionQuantity;
    private Boolean isSelected;
    private List<OptionTraitDetail> optionTraitDetails;

//    private Double calories;
//    private CountType countType;
//    private Integer defaultQuantity;
//    private Double extraPrice;
//    private String imageSource;
//    private Boolean isDefault;
//    private Integer maxQuantity;
//    private MeasureType measureType;
//    private String name;
//    private Integer orderIndex;

}

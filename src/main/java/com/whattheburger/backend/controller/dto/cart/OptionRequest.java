package com.whattheburger.backend.controller.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionRequest {
    private Long productOptionId;
    private Integer optionQuantity;
    private Boolean isSelected;
    private List<OptionTraitRequest> optionTraitRequests;

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

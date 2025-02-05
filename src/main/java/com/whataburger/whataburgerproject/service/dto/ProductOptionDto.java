package com.whataburger.whataburgerproject.service.dto;

import com.whataburger.whataburgerproject.domain.Option;
import com.whataburger.whataburgerproject.domain.ProductOptionTrait;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductOptionDto {
    private Option option;
    private ProductOptionTrait productOptionTrait;
}

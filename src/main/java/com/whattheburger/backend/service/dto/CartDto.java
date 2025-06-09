package com.whattheburger.backend.service.dto;

import com.whattheburger.backend.controller.dto.cart.SelectionDetail;
import com.whattheburger.backend.controller.dto.cart.ProductDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartDto {
    private ProductDetail productDetail;
    private SelectionDetail selectionDetail;
}

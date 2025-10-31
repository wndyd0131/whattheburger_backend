package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.CategorizedStoreProductsReadDto;
import com.whattheburger.backend.controller.dto.store.StoreProductCreateRequestDto;
import com.whattheburger.backend.controller.dto.store.StoreProductModifyRequestDto;
import com.whattheburger.backend.controller.dto.store.StoreProductCreateResponseDto;
import com.whattheburger.backend.controller.dto.store.StoreProductReadResponseDto;
import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.service.StoreProductService;
import com.whattheburger.backend.service.dto.ProductReadByCategoryIdResponseDto;
import com.whattheburger.backend.service.dto.StoreProductReadByProductIdDto;
import com.whattheburger.backend.service.dto.store.StoreProductsReadDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StoreProductController {
    private final StoreProductService storeProductService;

    @PostMapping("/api/v1/store/product")
    public ResponseEntity<String> createProduct(@RequestBody StoreProductCreateRequestDto storeProductDto) {
        Long productId = storeProductDto.getProductId();
        List<Long> storeIds = storeProductDto.getStoreIds();
        storeProductService.registerProductToStores(productId, storeIds);
        return new ResponseEntity<>(
                "StoreProducts successfully created",
                HttpStatus.CREATED
        );
        // Admin
        //
    }

    @GetMapping("/api/v1/store/{storeId}/category/product")
    public ResponseEntity<List<CategorizedStoreProductsReadDto>> fetchCategorizedStoreProducts(
            @PathVariable(name = "storeId") Long storeId
    ) {
        List<CategorizedStoreProductsReadDto> categorizedStoreProducts = storeProductService.getCategorizedStoreProducts(storeId);
        return ResponseEntity.ok(categorizedStoreProducts);
    }

    @GetMapping("/api/v1/store/{storeId}/product")
    public ResponseEntity<List<StoreProductsReadDto>> fetchAllStoreProducts(
            @PathVariable(name = "storeId") Long storeId
    ) {
        log.info("STORE ID {}", storeId);
        List<StoreProductsReadDto> storeProductsDto = storeProductService.getStoreProducts(storeId);
        return ResponseEntity.ok(storeProductsDto);
    }

    @GetMapping("/api/v1/store/{storeId}/product/{storeProductId}")
    public ResponseEntity<StoreProductReadByProductIdDto> fetchStoreProduct(
            @PathVariable(name = "storeId") Long storeId,
            @PathVariable(name = "storeProductId") Long storeProductId
    ) {
        StoreProductReadByProductIdDto storeProductDto = storeProductService.getProductById(storeId, storeProductId);
        return ResponseEntity.ok(storeProductDto);
    }
    @PostMapping("/api/v1/store/{storeId}/product/{storeProductId}")
    public void modifyProduct(
            @PathVariable Long storeId,
            @PathVariable Long storeProductId,
            @RequestBody StoreProductModifyRequestDto storeProductDto
    ) {
        storeProductService.modifyProduct(storeId, storeProductId, storeProductDto);

        // delta info (create api -> backend diff checking)
    }
}

package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO;
import com.whataburger.whataburgerproject.controller.dto.ProductCreateResponseDTO;
import com.whataburger.whataburgerproject.controller.dto.ProductReadByCategoryIdResponseDto;
import com.whataburger.whataburgerproject.controller.dto.ProductReadByProductIdResponseDto;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.domain.ProductOption;
import com.whataburger.whataburgerproject.domain.ProductOptionTrait;
import com.whataburger.whataburgerproject.service.ProductService;
import com.whataburger.whataburgerproject.service.dto.ProductReadByCategoryIdDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    @GetMapping("/api/v1/products")
    public List<Product> getAllProducts() {
        List<Product> allProducts = productService.getAllProducts();
        return allProducts;
    }
//    Product{
//        Option{
//            OptionTraits {
//
//            }
//            OptionTraits {
//
//            }
//        }
//        Option{
//
//        }
//    }


    // productOption -> Option & ProductOptionTrait info -> trying to make a structure of DTO, stuck with putting data into List
    @GetMapping("/api/v1/products/{productId}")
    public ProductReadByProductIdResponseDto getProductById(@PathVariable("productId") Long productId) {
        Product product = productService.findProductById(productId);
        List<ProductOption> productOptions = productService.findProductOptionByProductId(product.getId());

        for (ProductOption productOption : productOptions) {
            // productOption
            List<ProductOptionTrait> productOptionTraits = productOption.getProductOptionTraits();
            for (ProductOptionTrait productOptionTrait : productOptionTraits) {
//                productOptionTrait.getOptionTrait().getId();
//                productOptionTrait.getOptionTrait().getName();
            }
            ProductReadByProductIdResponseDto
                    .OptionTraitRequest
                    .builder()
                    .optionTraitId(product)
                    .build();
            ProductReadByProductIdResponseDto.OptionRequest.builder().optionTraitRequests()
            productOption.
        }
        return new ProductReadByProductIdResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageSource(),
                product.getIngredientInfo()
        );
    }

    @GetMapping("/api/v1/products/category/{categoryId}")
    public List<ProductReadByCategoryIdResponseDto> getProductsByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<ProductReadByCategoryIdDto> productsByCategoryId = productService.findProductsByCategoryId(categoryId);
        List<ProductReadByCategoryIdResponseDto> productReadResponseDto = new ArrayList<>();
        for (ProductReadByCategoryIdDto productDto : productsByCategoryId) {
            productReadResponseDto.add(
                    new ProductReadByCategoryIdResponseDto(
                            productDto.getProductId(),
                            productDto.getProductName(),
                            productDto.getProductPrice(),
                            productDto.getImageSource(),
                            productDto.getIngredientInfo()
                    )
            );
        }
        return productReadResponseDto;
    }

    @PostMapping("/api/v1/products")
    public ProductCreateResponseDTO createProduct(@RequestBody ProductCreateRequestDTO productCreateRequestDTO) {
        Product product = productService.createProduct(productCreateRequestDTO);
        return new ProductCreateResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getIngredientInfo(),
                product.getImageSource()
        );
    }
}

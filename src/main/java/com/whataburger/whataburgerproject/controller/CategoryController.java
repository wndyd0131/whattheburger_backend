package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.CategoryReadResponseDto;
import com.whataburger.whataburgerproject.service.CategoryService;
import com.whataburger.whataburgerproject.service.dto.CategoryReadDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/api/v1/category")
    public List<CategoryReadResponseDto> getAllCategories() {
        List<CategoryReadDto> allCategories = categoryService.findAllCategories();
        List<CategoryReadResponseDto> categoryReadResponseDtoList = new ArrayList<>();
        for (CategoryReadDto categoryReadDto : allCategories) {
            categoryReadResponseDtoList.add(
                    new CategoryReadResponseDto(
                        categoryReadDto.getCategoryId(),
                        categoryReadDto.getCategoryName(),
                        categoryReadDto.getCategoryImageSource(),
                        categoryReadDto.getProductCount()
                    )
            );
        }
        return categoryReadResponseDtoList;
    }
}

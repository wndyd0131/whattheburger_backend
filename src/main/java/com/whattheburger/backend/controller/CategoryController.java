package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.CategoryReadResponseDto;
import com.whattheburger.backend.service.CategoryService;
import com.whattheburger.backend.service.dto.CategoryReadDto;
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
        List<CategoryReadDto> allCategories = categoryService.getAllCategories();
        List<CategoryReadResponseDto> categoryReadResponseDtoList = new ArrayList<>();
        for (CategoryReadDto categoryReadDto : allCategories) {
            categoryReadResponseDtoList.add(
                    new CategoryReadResponseDto(
                        categoryReadDto.getCategoryId(),
                        categoryReadDto.getCategoryName(),
                        categoryReadDto.getProductCount()
                    )
            );
        }
        return categoryReadResponseDtoList;
    }
}

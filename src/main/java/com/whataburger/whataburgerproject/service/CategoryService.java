package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.Category;
import com.whataburger.whataburgerproject.repository.CategoryProductRepository;
import com.whataburger.whataburgerproject.repository.CategoryRepository;
import com.whataburger.whataburgerproject.service.dto.CategoryReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryProductRepository categoryProductRepository;

    public List<CategoryReadDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryReadDto> categoryReadDtoList = new ArrayList<>();
        for (Category category: categories) {
            int productCount = categoryProductRepository.findByCategoryId(category.getId()).size();
            categoryReadDtoList.add(
                    new CategoryReadDto(
                            category.getId(),
                            category.getName(),
                            productCount
                    )
            );
        }
        return categoryReadDtoList;
    }
}

package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.Category;
import com.whataburger.whataburgerproject.repository.CategoryRepository;
import com.whataburger.whataburgerproject.service.dto.CategoryReadDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    CategoryRepository categoryRepository;
    @InjectMocks
    CategoryService categoryService;

    @Test
    void categoryService_findAllCategories_returnAllCategoryDto() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("burgers"));
        categories.add(new Category("chickens"));
        when(categoryRepository.findAll()).thenReturn(categories);
        List<CategoryReadDto> allCategories = categoryService.getAllCategories();
        Assertions.assertThat(allCategories).isNotNull();
        Assertions.assertThat(allCategories.size()).isEqualTo(2);
    }
}

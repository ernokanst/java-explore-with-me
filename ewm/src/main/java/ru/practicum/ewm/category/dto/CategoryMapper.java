package ru.practicum.ewm.category.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.model.Category;

@Component
public class CategoryMapper {
    public Category toCategory(NewCategoryDto category) {
        return new Category(null, category.getName());
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}

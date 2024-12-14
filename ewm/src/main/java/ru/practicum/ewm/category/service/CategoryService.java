package ru.practicum.ewm.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.category.dto.*;
import java.util.List;

public interface CategoryService {
    CategoryDto add(NewCategoryDto category);

    CategoryDto update(CategoryDto category, Long id);

    List<CategoryDto> getAll(Pageable pageable);

    CategoryDto get(Long id);

    void delete(Long id);
}

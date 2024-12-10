package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.*;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.storage.CategoryRepository;
import ru.practicum.ewm.event.storage.EventRepository;
import ru.practicum.ewm.exceptions.NotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto add(NewCategoryDto category) {
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(category)));
    }

    @Override
    public CategoryDto update(CategoryDto category, Long id) {
        Category newCategory = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Категория не найдена"));
        newCategory.setName(category.getName());
        return categoryMapper.toCategoryDto(categoryRepository.save(newCategory));
    }

    @Override
    public List<CategoryDto> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).getContent().stream().map(categoryMapper::toCategoryDto).toList();
    }

    @Override
    public CategoryDto get(Long id) {
        return categoryMapper.toCategoryDto(categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена")));
    }

    @Override
    public void delete(Long id) {
        if (eventRepository.existsByCategoryId(id)) {
            throw new DataIntegrityViolationException("Существуют события с этой категорией");
        }
        categoryRepository.deleteById(id);
    }
}

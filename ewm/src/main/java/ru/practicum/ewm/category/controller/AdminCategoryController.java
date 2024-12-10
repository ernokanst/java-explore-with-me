package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.*;
import ru.practicum.ewm.category.service.CategoryService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto add(@RequestBody @Valid NewCategoryDto category) {
        log.info("Добавление новой категории: {}", category);
        return categoryService.add(category);
    }

    @PatchMapping("/{id}")
    public CategoryDto update(@RequestBody @Valid CategoryDto category, @PathVariable Long id) {
        log.info("Обновление категории id={}", id);
        return categoryService.update(category, id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Удаление категории id={}", id);
        categoryService.delete(id);
    }
}

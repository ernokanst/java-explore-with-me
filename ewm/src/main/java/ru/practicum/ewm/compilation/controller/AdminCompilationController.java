package ru.practicum.ewm.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.*;
import ru.practicum.ewm.compilation.service.CompilationService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/compilations")
public class AdminCompilationController {
    private final CompilationService compilationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping()
    public CompilationDto add(@RequestBody @Valid NewCompilationDto compilation) {
        log.info("Добавление новой подборки: {}", compilation);
        return compilationService.add(compilation);
    }

    @PatchMapping("/{id}")
    public CompilationDto update(@PathVariable Long id, @RequestBody @Valid UpdateCompilationRequest update) {
        log.info("Обновление подборки id={}", id);
        return compilationService.update(id, update);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Удаление подборки id={}", id);
        compilationService.delete(id);
    }
}

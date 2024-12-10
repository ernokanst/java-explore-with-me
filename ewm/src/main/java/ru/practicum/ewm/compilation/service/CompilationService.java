package ru.practicum.ewm.compilation.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.compilation.dto.*;
import java.util.List;

public interface CompilationService {
    CompilationDto add(NewCompilationDto compilation);

    CompilationDto update(Long id, UpdateCompilationRequest update);

    List<CompilationDto> getAll(Boolean pinned, Pageable pageable);

    CompilationDto get(Long id);

    void delete(Long id);
}

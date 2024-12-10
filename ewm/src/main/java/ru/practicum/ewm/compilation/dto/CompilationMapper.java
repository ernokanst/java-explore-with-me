package ru.practicum.ewm.compilation.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.model.Event;
import java.util.List;

@Component
public class CompilationMapper {
    public Compilation toCompilation(NewCompilationDto compilation, List<Event> events) {
        return new Compilation(null, events, compilation.getPinned() != null ? compilation.getPinned() : false, compilation.getTitle());
    }

    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return new CompilationDto(compilation.getId(), events, compilation.getPinned(), compilation.getTitle());
    }
}

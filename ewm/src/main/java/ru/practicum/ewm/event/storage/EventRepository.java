package ru.practicum.ewm.event.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.event.model.EventState;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(Long initiator, Pageable pageable);

    Boolean existsByCategoryId(Long id);

    List<Event> findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateBetween(List<Long> users, List<String> states,
                                                                                List<Long> categories,
                                                                                LocalDateTime rangeStart,
                                                                                LocalDateTime rangeEnd, Pageable pageable);

    List<Event> findByCategoryIdInAndEventDateBetweenAndStateIs(List<Long> categories, LocalDateTime rangeStart,
                                                                LocalDateTime rangeEnd, EventState state, Pageable pageable);
}

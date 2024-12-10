package ru.practicum.ewm.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.List;
import ru.practicum.ewm.event.model.Event;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "compilations")
public class Compilation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation"), inverseJoinColumns = @JoinColumn(name = "event"))
    private List<Event> events;
    @Column(name = "pinned", nullable = false)
    private Boolean pinned;
    @Column(name = "title", nullable = false)
    private String title;
}

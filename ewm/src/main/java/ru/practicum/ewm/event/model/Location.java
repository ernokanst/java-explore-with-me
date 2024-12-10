package ru.practicum.ewm.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location implements Serializable {
    @Column(name = "location_lat")
    private Float lat;
    @Column(name = "location_lon")
    private Float lon;
}

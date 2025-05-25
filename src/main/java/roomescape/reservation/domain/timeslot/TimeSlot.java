package roomescape.reservation.domain.timeslot;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class TimeSlot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    public TimeSlot(final LocalTime startAt) {
        this(null, startAt);
    }

    public boolean isBefore(final LocalTime other) {
        return startAt.isBefore(other) || startAt.equals(other);
    }
}

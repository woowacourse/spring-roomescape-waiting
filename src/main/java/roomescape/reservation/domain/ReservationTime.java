package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Duration;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public boolean hasConflict(final Duration duration, final LocalTime anotherTime) {
        final LocalTime endAt = startAt.plus(duration);
        return anotherTime.plus(duration).isAfter(startAt) && anotherTime.isBefore(endAt);
    }
}

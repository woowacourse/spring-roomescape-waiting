package roomescape.reservationtime.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Entity
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReservationTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private final Long id;

    @Column(nullable = false)
    private final LocalTime startAt;

    protected ReservationTime() {
        this(null, null);
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public boolean isBefore(final LocalTime localTime) {
        return startAt.isBefore(localTime);
    }
}

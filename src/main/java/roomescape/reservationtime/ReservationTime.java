package roomescape.reservationtime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalTime;

@Entity
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private final LocalTime startAt;

    public ReservationTime() {
        this.startAt = null;
    }

    public ReservationTime(final LocalTime startAt) {
        this.startAt = startAt;
    }

    public boolean isBefore(final LocalTime localTime) {
        return startAt.isBefore(localTime);
    }
}

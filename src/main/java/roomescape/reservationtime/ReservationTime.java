package roomescape.reservationtime;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReservationTime that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}

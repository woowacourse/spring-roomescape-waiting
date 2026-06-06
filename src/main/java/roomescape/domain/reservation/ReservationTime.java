package roomescape.domain.reservation;

import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public static ReservationTime create(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime of(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public boolean isBefore(LocalTime compareTime) {
        return startAt.isBefore(compareTime);
    }
}

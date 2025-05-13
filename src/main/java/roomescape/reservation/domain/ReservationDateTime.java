package roomescape.reservation.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.reservationTime.domain.ReservationTime;

public class ReservationDateTime {

    private final ReservationDate date;
    private final ReservationTime time;

    public ReservationDateTime(final ReservationDate date, final ReservationTime time) {
        this.date = Objects.requireNonNull(date, "date는 null 일 수 없습니다.");
        this.time = Objects.requireNonNull(time, "time은 null 일 수 없습니다.");
    }

    public boolean isBefore(final LocalDateTime dateTime) {
        return this.date.isBefore(dateTime.toLocalDate()) && this.time.isBefore(dateTime.toLocalTime());
    }
}

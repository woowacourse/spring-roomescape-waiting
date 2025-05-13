package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.time.ReservationTime;

public class ReservationDateTime {

    private final ReservationDate reservationDate;
    private final ReservationTime reservationTime;

    public ReservationDateTime(
            final ReservationDate reservationDate,
            final ReservationTime reservationTime,
            final Clock clock
    ) {
        this.reservationDate = Objects.requireNonNull(reservationDate, "reservationDate는 null일 수 없습니다.");
        this.reservationTime = Objects.requireNonNull(reservationTime, "reservationTime은 null일 수 없습니다.");
        Objects.requireNonNull(clock, "clock은 null일 수 없습니다.");
        validatePast(this.reservationDate, this.reservationTime, clock);
    }

    private void validatePast(
            final ReservationDate reservationDate,
            final ReservationTime reservationTime,
            final Clock clock
    ) {
        LocalDateTime reservationDateTime = LocalDateTime.of(
                reservationDate.getDate(),
                reservationTime.getStartAt()
        );
        LocalDateTime now = LocalDateTime.now(clock);

        if (reservationDateTime.isBefore(now)) {
            throw new IllegalStateException("현재 시간 이후로만 예약할 수 있습니다.");
        }
    }

    public ReservationDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Long getTimeId() {
        return reservationTime.getId();
    }
}

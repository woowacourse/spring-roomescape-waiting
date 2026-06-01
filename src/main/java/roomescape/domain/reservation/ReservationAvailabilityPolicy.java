package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.domain.reservationtime.ReservationTime;

@Component
public class ReservationAvailabilityPolicy {
    public static final String PAST_RESERVATION_MESSAGE = "과거 날짜와 시간으로는 예약을 할 수 없습니다.";
    public static final String PAST_WAITING_MESSAGE = "지난 예약에는 대기를 생성할 수 없습니다.";

    public void validateReservable(
            final LocalDate date,
            final ReservationTime reservationTime,
            final LocalDateTime standardDateTime
    ) {
        if (!isReservable(date, reservationTime, standardDateTime)) {
            throw new IllegalArgumentException(PAST_RESERVATION_MESSAGE);
        }
    }

    public void validateWaitable(final Reservation reservation, final LocalDateTime standardDateTime) {
        if (isPast(reservation, standardDateTime)) {
            throw new IllegalArgumentException(PAST_WAITING_MESSAGE);
        }
    }

    public boolean isReservable(
            final LocalDate date,
            final ReservationTime reservationTime,
            final LocalDateTime standardDateTime
    ) {
        return !isPast(date, reservationTime, standardDateTime);
    }

    public boolean isPast(final Reservation reservation, final LocalDateTime standardDateTime) {
        return isPast(reservation.getDate(), reservation.getTime(), standardDateTime);
    }

    public boolean isPast(
            final LocalDate date,
            final ReservationTime reservationTime,
            final LocalDateTime standardDateTime
    ) {
        return isPast(date, reservationTime.getStartAt(), standardDateTime);
    }

    public boolean isPast(
            final LocalDate date,
            final LocalTime startAt,
            final LocalDateTime standardDateTime
    ) {
        return LocalDateTime.of(date, startAt).isBefore(standardDateTime);
    }
}

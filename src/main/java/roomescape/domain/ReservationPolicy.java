package roomescape.domain;

import java.time.Clock;
import org.springframework.stereotype.Component;
import roomescape.exception.UnAvailableReservationException;

@Component
public class ReservationPolicy {

    private final Clock clock;

    public ReservationPolicy(Clock clock) {
        this.clock = clock;
    }

    public void validateReservationAvailable(final Reservation reservation, final boolean existsDuplicatedReservation) {
        validateUniqueReservation(existsDuplicatedReservation);
        validateNotPast(reservation);
        validateNotTooCloseFromNow(reservation);
    }

    private void validateUniqueReservation(final boolean existsDuplicatedReservation) {
        if (existsDuplicatedReservation) {
            throw new UnAvailableReservationException("테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.");
        }
    }

    private void validateNotTooCloseFromNow(Reservation reservation) {
        if (reservation.calculateMinutesUntilStart(clock) < 10) {
            throw new UnAvailableReservationException("예약 시간까지 10분도 남지 않아 예약이 불가합니다.");
        }
    }

    private void validateNotPast(final Reservation reservation) {
        if (reservation.isPast(clock)) {
            throw new UnAvailableReservationException("지난 날짜와 시간에 대한 예약은 불가능합니다.");
        }
    }
}

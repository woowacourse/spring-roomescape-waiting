package roomescape.domain;

import java.time.Clock;
import org.springframework.stereotype.Component;
import roomescape.exception.UnAvailableReservationException;

@Component
public class ReservationPolicy {
    private static final String PAST_RESERVATION_MESSAGE = "지난 날짜와 시간에 대한 예약은 불가능합니다.";
    private static final String TOO_SOON_RESERVATION_MESSAGE = "예약 시간까지 10분도 남지 않아 예약이 불가합니다.";
    private static final String DUPLICATE_RESERVATION_MESSAGE = "테마에 대해 날짜와 시간이 중복된 예약이 존재합니다.";

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
            throw new UnAvailableReservationException(DUPLICATE_RESERVATION_MESSAGE);
        }
    }

    private void validateNotPast(final Reservation reservation) {
        if (reservation.isPast(clock)) {
            throw new UnAvailableReservationException(PAST_RESERVATION_MESSAGE);
        }
    }

    private void validateNotTooCloseFromNow(Reservation reservation) {
        if (reservation.calculateMinutesUntilStart(clock) < 10) {
            throw new UnAvailableReservationException(TOO_SOON_RESERVATION_MESSAGE);
        }
    }
}

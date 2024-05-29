package roomescape.reservation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.exception.ReservationExceptionCode;

class DateTest {

    private static final int RESERVATION_POSSIBLE_MAX_DATE = 7;

    @Test
    @DisplayName("지난 날짜일 경우 에러를 발생한다.")
    void throwException_WhenPastDate() {
        Throwable pastDate = assertThrows(RoomEscapeException.class,
                () -> Date.saveFrom(LocalDate.MIN));

        assertEquals(pastDate.getMessage(), ReservationExceptionCode.RESERVATION_DATE_IS_PAST_EXCEPTION.getMessage());
    }

    @Test
    @DisplayName("예약 가능한 기간을 넘는 경우 에러를 발생한다.")
    void throwException_WhenOverMaxRangeReservationDate() {
        Throwable overRangeReservation = assertThrows(RoomEscapeException.class,
                () -> Date.saveFrom(LocalDate.now().plusDays(RESERVATION_POSSIBLE_MAX_DATE + 1)));

        assertEquals(overRangeReservation.getMessage(),
                ReservationExceptionCode.RESERVATION_DATE_IS_OVER_RANGE_EXCEPTION.getMessage());
    }
}

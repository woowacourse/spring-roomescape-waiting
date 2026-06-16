package roomescape.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.RoomescapeException;
import roomescape.reservation.ReservationStatus;
import roomescape.time.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.global.exception.ErrorCode.FORBIDDEN_RESERVATION_ACCESS;

class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation("ever", 1L, LocalDate.now(), new ReservationTime(1L, LocalTime.now().plusHours(1)), ReservationStatus.CONFIRMED);
    }

    @Test
    void 이름이_같으면_예외_발생하지_않음() {
        assertThatNoException()
                .isThrownBy(() -> reservation.validateSameName("ever"));
    }

    @Test
    void 이름이_다르면_예외_발생() {
        assertThatThrownBy(() -> reservation.validateSameName("other"))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(FORBIDDEN_RESERVATION_ACCESS.getMessage());
    }
}

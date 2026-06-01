package roomescape.waiting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;
import roomescape.time.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.exception.ErrorCode.FORBIDDEN_RESERVATION_WAITING_ACCESS;

class ReservationWaitingTest {

    private ReservationWaiting reservationWaiting;

    @BeforeEach
    void setUp() {
        reservationWaiting = new ReservationWaiting("ever", 1L, LocalDate.now(),
                new ReservationTime(LocalTime.now().plusHours(1)), LocalDateTime.now());
    }

    @Test
    void 이름이_같으면_예외_발생하지_않음() {
        assertThatNoException()
                .isThrownBy(() -> reservationWaiting.validateSameName("ever"));
    }

    @Test
    void 이름이_다르면_예외_발생() {
        assertThatThrownBy(() -> reservationWaiting.validateSameName("other"))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(FORBIDDEN_RESERVATION_WAITING_ACCESS.getMessage());
    }
}

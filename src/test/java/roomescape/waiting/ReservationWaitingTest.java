package roomescape.waiting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ErrorCode;
import roomescape.global.exception.RoomescapeException;
import roomescape.time.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static roomescape.global.exception.ErrorCode.*;

class ReservationWaitingTest {

    private ReservationWaiting reservationWaiting;

    @BeforeEach
    void setUp() {
        reservationWaiting = new ReservationWaiting("ever", 1L, LocalDate.now(), new ReservationTime(LocalTime.now().plusHours(1)), 1L);
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
                .hasMessage(UNAUTHORIZED_RESERVATION_WAITING_ACCESS.getMessage());
    }
}
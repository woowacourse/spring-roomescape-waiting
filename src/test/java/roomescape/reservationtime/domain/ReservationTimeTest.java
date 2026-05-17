package roomescape.reservationtime.domain;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservationtime.exeption.ReservationTimeErrorCode.*;

class ReservationTimeTest {

    @Test
    void 예약_시간이_null이면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> new ReservationTime(null),
                INVALID_RESERVATION_TIME
        );
    }

    @Test
    void 예약_시간_id가_null이면_도메인_예외가_발생한다() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));

        assertDomainException(
                () -> reservationTime.withId(null),
                INVALID_RESERVATION_TIME_ID
        );
    }

    @Test
    void 이미_id가_있는_예약_시간에_id를_부여하면_도메인_예외가_발생한다() {
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        assertDomainException(
                () -> reservationTime.withId(2L),
                RESERVATION_TIME_ALREADY_HAS_ID
        );
    }

    private void assertDomainException(Runnable runnable, ErrorPolicy errorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getErrorPolicy()).isEqualTo(errorCode)
                )
                .hasMessage(errorCode.message());
    }
}

package roomescape.reservationtime.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidRequestException;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeTest {

    @Test
    @DisplayName("예약 시간이 null이면 도메인 예외가 발생한다.")
    void create_fail_whenStartAtIsNull() {
        assertInvalidRequestException(
                () -> ReservationTime.create(null)
        );
    }

    @Test
    @DisplayName("예약 시간 id가 null이면 도메인 예외가 발생한다.")
    void withId_fail_whenIdIsNull() {
        ReservationTime reservationTime = ReservationTime.create(LocalTime.of(10, 0));

        assertInvalidRequestException(
                () -> reservationTime.withId(null)
        );
    }

    @Test
    @DisplayName("이미 id가 있는 예약 시간에 id를 부여하면 도메인 예외가 발생한다.")
    void withId_fail_whenReservationTimeAlreadyHasId() {
        ReservationTime reservationTime = ReservationTime.create(LocalTime.of(10, 0)).withId(1L);

        assertInvalidRequestException(
                () -> reservationTime.withId(2L)
        );
    }

    private void assertInvalidRequestException(Runnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(InvalidRequestException.class);
    }
}

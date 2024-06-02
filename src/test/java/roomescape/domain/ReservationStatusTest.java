package roomescape.domain;

import static roomescape.domain.ReservationStatus.Status.RESERVED;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.ReservationStatus.Status;
import roomescape.exception.wait.InvalidPriorityException;

class ReservationStatusTest {

    @Test
    @DisplayName("우선순위가 0이면 예약 상태로 생성된다")
    void ReservationStatus_ShouldGenerateWithReserved() {
        // given
        long priority = 0L;

        // when
        ReservationStatus reservationStatus = new ReservationStatus(priority);

        // then
        Assertions.assertThat(reservationStatus.getStatus()).isSameAs(RESERVED);
    }


    @Test
    @DisplayName("우선순위가 1 이상이면 대기 상태로 생성된다")
    void ReservationStatus_ShouldGenerateWithWaiting() {
        // given
        long priority = 1L;

        // when
        ReservationStatus reservationStatus = new ReservationStatus(priority);

        // then
        Assertions.assertThat(reservationStatus.getStatus()).isSameAs(Status.WAITING);
    }

    @Test
    @DisplayName("우선순위가 0 미만이면 예외를 발생시칸다")
    void ReservationStatus_ShouldThrowException_WhenPriorityLessThen0() {
        // given
        long priority = -1L;

        // when & then
        Assertions.assertThatThrownBy(() -> new ReservationStatus(priority))
                .isInstanceOf(InvalidPriorityException.class);
    }
}

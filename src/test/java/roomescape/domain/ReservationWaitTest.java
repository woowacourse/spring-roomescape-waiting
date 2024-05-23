package roomescape.domain;

import static roomescape.domain.ReservationStatus.RESERVED;
import static roomescape.domain.ReservationStatus.WAITING;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationWaitTest {

    @Test
    @DisplayName("예약 대기를 생성한다")
    void ReservationWait_Generate() {
        // when & then
        Assertions.assertThatCode(() -> new ReservationWait(null, null, 0, RESERVED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 대기 도메인은 우선순위가 0일때 예약중인 상태를 뜻한다")
    void ReservationWait_ShouldThrowException_WhenPriority0ButStatusIsNotRESERVED() {
        // when & then
        Assertions.assertThatThrownBy(() -> new ReservationWait(null, null, 0, WAITING))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 대기 도메인은 우선순위가 0이 아닐때 예약중인 상태면 예외를 발생한다")
    void ReservationWait_ShouldThrowException_WhenPriorityGreaterThen0ButStatusIsRESERVED() {
        // when & then
        Assertions.assertThatThrownBy(() -> new ReservationWait(null, null, 1, RESERVED))
                .isInstanceOf(IllegalArgumentException.class);
    }


}

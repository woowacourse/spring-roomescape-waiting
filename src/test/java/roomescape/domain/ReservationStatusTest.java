package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationStatusTest {
    @DisplayName("예약 상태가 대기일 경우 참을 반환한다.")
    @Test
    void given_status_when_isWaiting_then_true() {
        //given
        ReservationStatus waiting = ReservationStatus.WAITING;
        //when
        boolean isWaiting = waiting.isWaiting();
        //then
        assertThat(isWaiting).isTrue();
    }
}

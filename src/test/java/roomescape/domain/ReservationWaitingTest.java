package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationWaitingTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new ReservationWaiting(VALID_MEMBER, VALID_RESERVATION, 1L))
                .doesNotThrowAnyException();
    }

    @DisplayName("대기 번호가 0 이하일 경우 예외가 발생한다.")
    @Test
    void createFail_NegativePriority() {
        assertThatThrownBy(() -> new ReservationWaiting(VALID_MEMBER, VALID_RESERVATION, 0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

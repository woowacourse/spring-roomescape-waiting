package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidReservationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationTimeTest {

    @DisplayName("올바르지 않은 시간으로 설정하면 예외를 던진다.")
    @Test
    void invalidDateSchedule() {
        //given
        String invalidTime = "";

        //when&then
        assertThatThrownBy(() -> new ReservationTime(1, invalidTime))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("올바르지 않은 시간입니다.");
    }
}

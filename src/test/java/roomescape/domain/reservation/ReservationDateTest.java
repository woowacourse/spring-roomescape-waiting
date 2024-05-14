package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidReservationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationDateTest {
    @DisplayName("일정은 올바르지 않은 날짜로 설정하면 예외를 던진다.")
    @Test
    void invalidDateSchedule() {
        //given
        String invalidDate = "";

        //when&then
        assertThatThrownBy(() -> new ReservationDate(invalidDate))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("올바르지 않은 날짜입니다.");
    }
}

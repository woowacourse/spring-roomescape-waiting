package roomescape.reservation.dto.user;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidDateException;
import roomescape.common.exception.InvalidIdException;

class UserReservationRequestTest {

    @DisplayName("날짜가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_date_null() {
        assertThatThrownBy(() -> new UserReservationRequest(null, 1L, 1L))
                .isInstanceOf(InvalidDateException.class);
    }

    @DisplayName("날짜가 현재보다 이전일 경우 예외가 발생한다.")
    @Test
    void exception_date_before() {
        assertThatThrownBy(() -> new UserReservationRequest(LocalDate.now().minusDays(1), 1L, 1L))
                .isInstanceOf(InvalidDateException.class);
    }

    @DisplayName("시간 id가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_timeId_null() {
        assertThatThrownBy(() -> new UserReservationRequest(LocalDate.now(), null, 1L))
                .isInstanceOf(InvalidIdException.class);
    }

    @DisplayName("테마 id가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_themeId_null() {
        assertThatThrownBy(() -> new UserReservationRequest(LocalDate.now(), 1L, null))
                .isInstanceOf(InvalidIdException.class);
    }
}

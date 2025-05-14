package roomescape.reservation.dto.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidDateException;
import roomescape.common.exception.InvalidIdException;

class AdminReservationSearchRequestTest {

    @DisplayName("멤버 id가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_memberId_null() {
        assertThatThrownBy(() -> new AdminReservationSearchRequest(null, 1L, LocalDate.now(), LocalDate.now()))
                .isInstanceOf(InvalidIdException.class);
    }

    @DisplayName("테마 id가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_themeId_null() {
        assertThatThrownBy(() -> new AdminReservationSearchRequest(1L, null, LocalDate.now(), LocalDate.now()))
                .isInstanceOf(InvalidIdException.class);
    }

    @DisplayName("시작 날짜가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_dateFrom_null() {
        assertThatThrownBy(() -> new AdminReservationSearchRequest(1L, 1L, null, LocalDate.now()))
                .isInstanceOf(InvalidDateException.class);
    }

    @DisplayName("종료 날짜가 널 값일 경우 예외가 발생한다.")
    @Test
    void exception_dateTo_null() {
        assertThatThrownBy(() -> new AdminReservationSearchRequest(1L, 1L, LocalDate.now(), null))
                .isInstanceOf(InvalidDateException.class);
    }
}

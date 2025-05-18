package roomescape.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.exception.custom.InvalidInputException;

class ReservationRequestTest {

    @Test
    @DisplayName("날짜는 빈 값이 들어올 수 없다.")
    void validateNullOfDate() {
        assertThatThrownBy(() -> new ReservationRequest(null, 1L, 1L, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }

    @Test
    @DisplayName("시간은 빈 값이 들어올 수 없다.")
    void validateNullOfTime() {
        assertThatThrownBy(() -> new ReservationRequest(LocalDate.of(2025, 4, 29), null, 1L, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }

    @Test
    @DisplayName("테마는 빈 값이 들어올 수 없다.")
    void validateNullOfTheme() {
        assertThatThrownBy(() -> new ReservationRequest(LocalDate.of(2025, 4, 29), 1L, null, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }
}

package roomescape.reservation.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.global.exception.InvalidInputException;

public class ReservationRequestTest {

    @Test
    @DisplayName("날짜는 빈 값이 들어올 수 없다.")
    void validateNullOfDate() {
        assertThatThrownBy(() -> new ReservationRequest(null, "프리", 1L, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 날짜가 입력되지 않았다.");
    }

    @Test
    @DisplayName("이름은 빈 값이 들어올 수 없다.")
    void validateNullOfName() {
        assertThatThrownBy(() -> new ReservationRequest(LocalDate.of(2025, 4, 29), null, 1L, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약자 이름이 입력되지 않았다.");
    }

    @Test
    @DisplayName("시간은 빈 값이 들어올 수 없다.")
    void validateNullOfTime() {
        assertThatThrownBy(() -> new ReservationRequest(LocalDate.of(2025, 4, 29), "프리", null, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 시간이 입력되지 않았다.");
    }

    @Test
    @DisplayName("테마는 빈 값이 들어올 수 없다.")
    void validateNullOfTheme() {
        assertThatThrownBy(() -> new ReservationRequest(LocalDate.of(2025, 4, 29), "사나", 1L, null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약할 테마가 입력되지 않았다.");
    }

    @Test
    @DisplayName("이름은 한 글자 이상이어야 한다")
    void validateNameLength() {
        assertThatThrownBy(() -> new ReservationRequest(LocalDate.of(2025, 4, 29), "", 1L, 1L))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("예약자 이름은 한 글자 이상이어야 한다.");
    }
}

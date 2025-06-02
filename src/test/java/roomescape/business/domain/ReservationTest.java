package roomescape.business.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    @DisplayName("date 필드에 null 들어오면 예외가 발생한다")
    void validateDate() {
        // given
        final LocalDate invalidDate = null;

        // when & then
        assertThatThrownBy(() -> new Reservation(
                invalidDate,
                new ReservationTime(LocalTime.of(14, 0)),
                new Theme("테마 이름", "테마 설명", "테마 썸네일")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

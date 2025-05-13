package roomescape.business.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    @DisplayName("date 필드에 null 들어오면 예외가 발생한다")
    void validateDate() {
        // given
        final LocalDate invalidDate = null;

        // when & then
        assertThatThrownBy(() -> new Reservation(invalidDate, new Member(1L), new PlayTime(1L), new Theme(1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}

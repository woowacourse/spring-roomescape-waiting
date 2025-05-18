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
        final Member member = new Member(1L, "name", "role", "email", "password");
        final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));
        final Theme theme = new Theme(1L, "name", "description", "thumbnail");

        // when & then
        assertThatThrownBy(() -> new Reservation(invalidDate, member, reservationTime, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

}

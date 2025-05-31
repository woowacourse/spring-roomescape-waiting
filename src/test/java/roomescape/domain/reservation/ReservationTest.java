package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRole;
import roomescape.exception.BusinessRuleViolationException;

class ReservationTest {


    @Test
    @DisplayName("과거 날짜로 예약을 시도하면 예외를 던진다.")
    void validateDateTime_WhenPastDate() {
        // given
        var user = User.ofExisting(2L, "사용자1", UserRole.USER, "user1@email.com", "password1");
        var timeSlot = TimeSlot.ofExisting(1L, LocalTime.of(10, 0));
        var theme = Theme.ofExisting(1L, "테마", "설명", "thumbnail");
        var pastDate = LocalDate.now().minusDays(1);

        // when & then
        assertThatThrownBy(() -> Reservation.register(user, pastDate, timeSlot, theme))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("이전 날짜로 예약할 수 없습니다.");
    }
}

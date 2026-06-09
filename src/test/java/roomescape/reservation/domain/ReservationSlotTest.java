package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidBusinessStateException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

class ReservationSlotTest {

    @Test
    @DisplayName("과거 날짜인 경우 예외가 발생한다.")
    void validateNotExpired_pastDate() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationSlot slot = new ReservationSlot(pastDate, time, theme);

        LocalDateTime requestTime = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> slot.validateNotExpired(requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_DATE.getMessage());
    }

    @Test
    @DisplayName("오늘이지만 이미 지난 시간인 경우 예외가 발생한다.")
    void validateNotExpired_pastTimeToday() {
        // given
        LocalDate today = LocalDate.now();
        LocalTime pastTime = LocalTime.of(10, 0);
        ReservationTime time = new ReservationTime(1L, pastTime);
        Theme theme = new Theme(1L, "테마", "설명", "url");
        ReservationSlot slot = new ReservationSlot(today, time, theme);

        LocalDateTime requestTime = today.atTime(11, 0);

        // when & then
        assertThatThrownBy(() -> slot.validateNotExpired(requestTime))
                .isInstanceOf(InvalidBusinessStateException.class)
                .hasMessage(ReservationErrorCode.INVALID_TIME.getMessage());
    }
}

package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

class ReservationSlotTest {

    @DisplayName("슬롯 시간이 기준 시각보다 이전이면 과거 슬롯이다")
    @Test
    void isPast() {
        ReservationSlot slot = new ReservationSlot(
                LocalDate.parse("2026-03-08"),
                createTheme(),
                ReservationTime.createNew(LocalTime.parse("10:00"))
        );

        boolean past = slot.isPast(LocalDateTime.parse("2026-03-08T10:01:00"));

        assertThat(past).isTrue();
    }

    @DisplayName("예약 날짜 null 예외")
    @Test
    void validateDate_ThrowsException() {
        Theme theme = createTheme();
        ReservationTime time = ReservationTime.createNew(LocalTime.parse("10:00"));

        assertThatThrownBy(() -> new ReservationSlot(null, theme, time))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 시간 null 예외")
    @Test
    void validateTime_ThrowsException() {
        Theme theme = createTheme();
        LocalDate date = LocalDate.parse("2026-03-08");

        assertThatThrownBy(() -> new ReservationSlot(date, theme, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Theme createTheme() {
        return Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
    }
}

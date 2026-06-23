package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;

class ReservationSlotTest {

    private static final TimeSlot TIME_SLOT = new TimeSlot(1L, LocalTime.of(10, 0));
    private static final Theme THEME = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");

    @Test
    @DisplayName("예약 슬롯은 날짜, 시간, 테마로 생성된다.")
    void 예약_슬롯_생성() {
        ReservationSlot slot = new ReservationSlot(LocalDate.now().plusDays(1), TIME_SLOT, THEME);

        assertThat(slot.getDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(slot.getTimeSlot()).isEqualTo(TIME_SLOT);
        assertThat(slot.getTheme()).isEqualTo(THEME);
    }

    @Test
    @DisplayName("예약 날짜가 null이면 예외가 발생한다.")
    void 예약_날짜_null_예외_발생() {
        assertThatThrownBy(() -> new ReservationSlot(null, TIME_SLOT, THEME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 날짜는 필수입니다.");
    }

    @Test
    @DisplayName("예약 시간이 null이면 예외가 발생한다.")
    void 예약_시간_null_예외_발생() {
        assertThatThrownBy(() -> new ReservationSlot(LocalDate.now().plusDays(1), null, THEME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간은 필수입니다.");
    }

    @Test
    @DisplayName("테마가 null이면 예외가 발생한다.")
    void 테마_null_예외_발생() {
        assertThatThrownBy(() -> new ReservationSlot(LocalDate.now().plusDays(1), TIME_SLOT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마는 필수입니다.");
    }

    @Test
    @DisplayName("기준 시각보다 슬롯 시간이 이전이면 지난 슬롯이다.")
    void 지난_슬롯_확인() {
        ReservationSlot slot = new ReservationSlot(
                LocalDate.of(2026, 6, 3),
                TIME_SLOT,
                THEME
        );

        assertThat(slot.isPast(LocalDateTime.of(2026, 6, 3, 11, 0))).isTrue();
    }
}

package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationSlotTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(1L, "방탈출1", "설명", "https://thumb.com");

    @Test
    void date가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationSlot(null, time, theme))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void time이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationSlot(LocalDate.of(2026, 6, 1), null, theme))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void theme이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationSlot(LocalDate.of(2026, 6, 1), time, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 슬롯의_날짜와_시간이_현재보다_이전이면_true를_반환한다() {
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 1), time, theme);
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 11, 0);

        assertThat(slot.isPast(now)).isTrue();
    }

    @Test
    void 슬롯의_날짜와_시간이_현재보다_이후이면_false를_반환한다() {
        ReservationSlot slot = new ReservationSlot(LocalDate.of(2026, 6, 1), time, theme);
        LocalDateTime now = LocalDateTime.of(2026, 6, 1, 9, 0);

        assertThat(slot.isPast(now)).isFalse();
    }
}

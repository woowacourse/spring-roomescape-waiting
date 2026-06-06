package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationSlotTest {

    private final LocalDate date = LocalDate.parse("2026-05-05");
    private final ReservationTime time = new ReservationTime(1L, LocalTime.parse("10:00"));
    private final Theme theme = new Theme(1L, "테마 이름", "테마 설명", "썸네일");

    @Test
    void 날짜가_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationSlot(null, time, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("date는 비어 있을 수 없습니다.");
    }

    @Test
    void 예약_시간이_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationSlot(date, null, theme))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("time은 비어있을 수 없습니다.");
    }

    @Test
    void 테마가_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationSlot(date, time, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("theme는 비어있을 수 없습니다.");
    }

    @Test
    void 지난_슬롯인지_확인한다() {
        // given
        ReservationSlot slot = new ReservationSlot(LocalDate.now().minusDays(1), time, theme);

        // when & then
        assertThat(slot.isPast(LocalDateTime.now())).isTrue();
    }

    @Test
    void 날짜와_시간이_같으면_같은_일정이다() {
        // given
        ReservationSlot slot = new ReservationSlot(date, time, theme);
        ReservationSlot sameSchedule = new ReservationSlot(
                date,
                time,
                new Theme(2L, "다른 테마", "다른 설명", "다른 썸네일"));
        ReservationSlot differentSchedule = new ReservationSlot(date.plusDays(1), time, theme);

        // when & then
        assertThat(slot.hasSameSchedule(sameSchedule)).isTrue();
        assertThat(slot.hasSameSchedule(differentSchedule)).isFalse();
    }
}

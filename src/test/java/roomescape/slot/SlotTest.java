package roomescape.slot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

class SlotTest {

    @Test
    @DisplayName("슬롯은 날짜, 시간, 테마 없이 생성될 수 없다.")
    void create_null_fail() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "theme", "description", "thumbnail");

        assertThatNullPointerException()
                .isThrownBy(() -> Slot.create(null, time, theme));
        assertThatNullPointerException()
                .isThrownBy(() -> Slot.create(LocalDate.of(2026, 5, 5), null, theme));
        assertThatNullPointerException()
                .isThrownBy(() -> Slot.create(LocalDate.of(2026, 5, 5), time, null));
    }

    @Test
    @DisplayName("슬롯 시작 시각이 현재보다 이전이면 과거 슬롯이다.")
    void isPast_true() {
        Slot slot = slot(LocalDate.of(2026, 5, 5), LocalTime.of(10, 0));

        assertThat(slot.isPast(LocalDateTime.of(2026, 5, 5, 10, 1))).isTrue();
    }

    @Test
    @DisplayName("슬롯 시작 시각이 현재와 같거나 이후이면 과거 슬롯이 아니다.")
    void isPast_false() {
        Slot slot = slot(LocalDate.of(2026, 5, 5), LocalTime.of(10, 0));

        assertThat(slot.isPast(LocalDateTime.of(2026, 5, 5, 10, 0))).isFalse();
        assertThat(slot.isPast(LocalDateTime.of(2026, 5, 5, 9, 59))).isFalse();
    }

    @Test
    @DisplayName("과거 슬롯이면 검증에 실패한다.")
    void validateNotPast_fail() {
        Slot slot = slot(LocalDate.of(2026, 5, 5), LocalTime.of(10, 0));

        assertThatThrownBy(() -> slot.validateNotPast(LocalDateTime.of(2026, 5, 5, 10, 1)))
                .isInstanceOf(EscapeRoomException.class);
    }

    private Slot slot(LocalDate date, LocalTime startAt) {
        return Slot.of(
                1L,
                date,
                new ReservationTime(1L, startAt),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }
}

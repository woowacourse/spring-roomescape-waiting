package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.PastReservationException;

class SlotTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 5);

    private Slot slot(LocalDate date, long timeId, long themeId) {
        return new Slot(
                date,
                ReservationTime.create(timeId, LocalTime.of(10, 0)),
                Theme.create(themeId, "테마", "url", "설명")
        );
    }

    @Test
    @DisplayName("날짜와 시간 id, 테마 id가 모두 같으면 같은 슬롯이다.")
    void isSameSlotTrue() {
        assertThat(slot(DATE, 1, 1).isSameSlot(slot(DATE, 1, 1))).isTrue();
    }

    @Test
    @DisplayName("날짜가 다르면 다른 슬롯이다.")
    void isSameSlotDifferentDate() {
        assertThat(slot(DATE, 1, 1).isSameSlot(slot(DATE.plusDays(1), 1, 1))).isFalse();
    }

    @Test
    @DisplayName("시간 id가 다르면 다른 슬롯이다.")
    void isSameSlotDifferentTime() {
        assertThat(slot(DATE, 1, 1).isSameSlot(slot(DATE, 2, 1))).isFalse();
    }

    @Test
    @DisplayName("테마 id가 다르면 다른 슬롯이다.")
    void isSameSlotDifferentTheme() {
        assertThat(slot(DATE, 1, 1).isSameSlot(slot(DATE, 1, 2))).isFalse();
    }

    @Test
    @DisplayName("과거 슬롯은 검증 시 예외를 던진다.")
    void validateNotPastDelegates() {
        Slot pastSlot = new Slot(DATE, ReservationTime.create(1, LocalTime.of(10, 0)), Theme.create(1, "t", "u", "d"));
        LocalDateTime now = LocalDateTime.of(2026, 6, 5, 12, 0);

        assertThatThrownBy(() -> pastSlot.validateNotPast(now))
                .isInstanceOf(PastReservationException.class);
    }
}

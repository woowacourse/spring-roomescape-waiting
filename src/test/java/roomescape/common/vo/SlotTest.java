package roomescape.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.store.Store;
import roomescape.theme.Theme;
import roomescape.time.Time;

class SlotTest {

    private final Theme theme = new Theme(new Name("테마"), "http://url", "설명");
    private final Store store = new Store(1L, "강남점");

    private Slot slotAt(LocalDate date, LocalTime startAt) {
        return new Slot(date, new Time(1L, startAt), theme, store);
    }

    @Nested
    class IsPast {

        @Test
        @DisplayName("예약 일시가 현재보다 과거이면 true를 반환한다")
        void returnsTrueWhenInPast() {
            Slot slot = slotAt(LocalDate.now().minusDays(1), LocalTime.of(10, 0));

            assertThat(slot.isPast(LocalDateTime.now())).isTrue();
        }

        @Test
        @DisplayName("예약 일시가 현재보다 미래이면 false를 반환한다")
        void returnsFalseWhenInFuture() {
            Slot slot = slotAt(LocalDate.now().plusDays(1), LocalTime.of(10, 0));

            assertThat(slot.isPast(LocalDateTime.now())).isFalse();
        }
    }
}

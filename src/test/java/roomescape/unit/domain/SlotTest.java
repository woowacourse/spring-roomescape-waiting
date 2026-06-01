package roomescape.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.BusinessRuleViolationException;

class SlotTest {

    private static final ReservationTime TWO_PM = new ReservationTime(1L, LocalTime.of(14, 0));
    private static final Theme THEME = new Theme(1L, "공포", "설명", "https://example.com/horror.jpg");

    @Test
    void 슬롯_시각이_현재보다_과거면_isPast가_true를_반환한다() {
        Slot slot = new Slot(LocalDate.of(2026, 5, 14), TWO_PM, THEME);

        assertThat(slot.isPast(LocalDateTime.of(2026, 5, 14, 14, 30))).isTrue();
    }

    @Test
    void 슬롯_시각이_현재와_같으면_isPast가_false를_반환한다() {
        Slot slot = new Slot(LocalDate.of(2026, 5, 14), TWO_PM, THEME);

        assertThat(slot.isPast(LocalDateTime.of(2026, 5, 14, 14, 0))).isFalse();
    }

    @Test
    void 슬롯은_날짜_시간_테마가_모두_필요하다() {
        assertThatThrownBy(() -> new Slot(null, TWO_PM, THEME))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("슬롯");
    }
}

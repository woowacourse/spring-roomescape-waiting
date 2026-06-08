package roomescape.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeTest {

    @Test
    void 유효한_시작_시간으로_예약_시간을_생성하면_필드가_저장된다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 시작_시간이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(DomainRuleViolationException.class)
                .hasMessage("예약 시작 시간은 비어 있을 수 없습니다.");
    }

    @Test
    void 주어진_날짜와_조합한_시점이_현재보다_과거면_isPast가_true이다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 5, 1, 12, 0);

        assertThat(time.isPast(LocalDate.of(2026, 5, 1), now)).isTrue();
    }

    @Test
    void 주어진_날짜와_조합한_시점이_현재보다_미래면_isPast가_false이다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDateTime now = LocalDateTime.of(2026, 5, 1, 8, 0);

        assertThat(time.isPast(LocalDate.of(2026, 5, 1), now)).isFalse();
    }
}

package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReservationTimeTest {

    @Test
    void 시작시간이_null이면_예약시간을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시작 시간은 비어 있을 수 없습니다.");
    }

    @Test
    void 지난_날짜의_슬롯은_과거다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(time.isPast(LocalDate.now().minusDays(1))).isTrue();
    }

    @Test
    void 다가올_날짜의_슬롯은_과거가_아니다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(time.isPast(LocalDate.now().plusDays(1))).isFalse();
    }
}

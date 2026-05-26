package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    void 시간에_초_단위가_포함되면_예외를_던진다() {
        // when // then
        assertThatThrownBy(() -> ReservationTime.of(1L, LocalTime.of(10, 0, 30)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 예약 시간은 분 단위까지만 입력해주세요.");
    }

    @Test
    void 시간_정보가_없다면_예외를_던진다() {
        // when // then
        assertThatThrownBy(() -> ReservationTime.of(1L, null))
                .isInstanceOf(NullPointerException.class);
    }
}

package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class ReservationTimeTest {

    @Test
    void 시작시간이_null이면_예약시간을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시작 시간은 비어 있을 수 없습니다.");
    }
}

package roomescape.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReservationTimeTest {

    @Test
    void 시작시간이_null이면_예약시간을_생성할_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeTest {

    @Test
    void null로_예약시간_생성시_예외() {
        // when & then
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startAt은 비어 있을 수 없습니다.");
    }

    @Test
    void 예약시간_생성_성공_테스트() {
        // given
        LocalTime startAt = LocalTime.parse("10:00");

        // when
        ReservationTime result = new ReservationTime(null, startAt);

        // then
        assertThat(result.getStartAt()).isEqualTo(startAt);
    }
}

package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class TimeAvailabilityTest {

    @Test
    void 시간_예약_가능_여부_생성_성공_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));

        // when
        TimeAvailability result = new TimeAvailability(time, true);

        // then
        assertAll(
                () -> assertThat(result.getTime()).isEqualTo(time),
                () -> assertThat(result.isAvailable()).isTrue());
    }

    @Test
    void 시간이_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new TimeAvailability(null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("time은 비어있을 수 없습니다.");
    }
}

package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

class TimeTest {

    @Test
    void 시간_생성() {
        Time time = new Time(1L, LocalTime.of(10, 0));
        assertThat(time.getId()).isEqualTo(1L);
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void 시작_시간이_null이면_예외() {
        assertThatThrownBy(() -> new Time(1L, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TIME_START_AT_NULL.getMessage());
    }
}

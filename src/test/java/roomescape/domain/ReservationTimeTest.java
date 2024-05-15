package roomescape.domain;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import roomescape.exception.BadRequestException;

class ReservationTimeTest {
    @DisplayName("시간에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyTime(LocalTime value) {
        Assertions.assertThatThrownBy(() -> new ReservationTime(value))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("시간에 빈값을 입력할 수 없습니다.");
    }
}

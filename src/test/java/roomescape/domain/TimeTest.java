package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

class TimeTest {

    @DisplayName("시간이 없으면 예외가 발생한다.")
    @Test
    void createExceptionTest() {
        assertThatCode(() -> new Time(null))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }
}

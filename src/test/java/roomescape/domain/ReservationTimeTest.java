package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.exception.ExceptionType.EMPTY_TIME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;

class ReservationTimeTest {
    @Test
    @DisplayName("시간이 비어있는 경우 생성할 수 없는지 확인")
    void createFailWhenEmptyStartAt() {
        assertThatThrownBy(() -> new ReservationTime(null))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_TIME.getMessage());
    }
}

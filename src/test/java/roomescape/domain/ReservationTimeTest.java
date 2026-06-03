package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("시작 시간이 null이면 예외")
    void throwsExceptionWhenStartAtIsNull() {
        assertThatThrownBy(() -> new ReservationTime(null, null))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_DOMAIN);
    }
}

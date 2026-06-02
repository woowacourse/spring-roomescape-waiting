package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.ReservationTimeErrorCode;
import roomescape.exception.RoomEscapeException;

class ReservationTimeTest {

    @Test
    void 시간이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReservationTime.create(null))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationTimeErrorCode.INVALID_TIME);
    }

    @Test
    void 정각이_아니면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReservationTime.create(LocalTime.parse("10:30")))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ReservationTimeErrorCode.INVALID_TIME);
    }
}

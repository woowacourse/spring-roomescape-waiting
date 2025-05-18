package roomescape.reservationTime.dto.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidTimeException;

class ReservationTimeRequestTest {

    @DisplayName("시작 시간이 널 값인 경우 예외가 발생한다.")
    @Test
    void exception_time_null() {
        assertThatThrownBy(() -> new ReservationTimeRequest(null))
                .isInstanceOf(InvalidTimeException.class);
    }
}

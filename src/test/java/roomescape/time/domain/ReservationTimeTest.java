package roomescape.time.domain;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationTimeTest {

    @Test
    void cannotNullTime() {
        // when & then
        assertThatThrownBy(() -> ReservationTime.withoutId(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("ReservationTime.value 은(는) null일 수 없습니다.");
    }
}

package roomescape.time.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;

class ReservationTimeTest {

    @Test
    void cannotNullTime() {
        // when & then
        assertThatThrownBy(() -> ReservationTime.withoutId(null))
                .isInstanceOf(InvalidInputException.class);
    }
}

package roomescape.reservation.domain;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationDateTest {

    @Test
    void cannotNullDate() {
        // When & Then
        assertThatThrownBy(() -> ReservationDate.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("ReservationDate.value 은(는) null일 수 없습니다.");
    }
}

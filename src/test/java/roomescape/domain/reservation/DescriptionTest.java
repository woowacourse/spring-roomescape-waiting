package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidReservationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DescriptionTest {
    @DisplayName("설명은 100자 초과일 경우 예외를 발생시킨다.")
    @Test
    void invalidDescriptionLength() {
        //given
        String description = "1".repeat(101);

        //when&then
        assertThatThrownBy(() -> new Description(description))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("설명은 100자를 초과할 수 없습니다.");
    }
}

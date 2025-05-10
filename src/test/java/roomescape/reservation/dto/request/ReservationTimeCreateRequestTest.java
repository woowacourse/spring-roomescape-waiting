package roomescape.reservation.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidInputException;

public class ReservationTimeCreateRequestTest {

    @Test
    @DisplayName("시간은 빈 값이 들어올 수 없다.")
    void validateNull() {
        assertThatThrownBy(() -> new ReservationTimeCreateRequest(null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessage("시간이 입력되지 않았다.");
    }
}

package roomescape.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.request.TimeRequest;
import roomescape.exception.custom.InvalidInputException;

class TimeRequestTest {

    @Test
    @DisplayName("시간은 빈 값이 들어올 수 없다.")
    void validateNullOfStartTime() {
        assertThatThrownBy(() -> new TimeRequest(null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }
}

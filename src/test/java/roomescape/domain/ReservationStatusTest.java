package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReservationStatusTest {

    @Test
    void 존재하지_않는_status_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReservationStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

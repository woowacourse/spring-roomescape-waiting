package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationSearchTest {
    @DisplayName("검색조건이 모두 null일 수는 없다.")
    @Test
    void validateTest_whenAllArgumentIsNull() {
        assertThatThrownBy(() -> new ReservationSearch(null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("검색 조건은 1개 이상 있어야 합니다.");
    }
}

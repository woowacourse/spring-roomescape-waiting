package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.WaitingWithRank;

class WaitingWithRankTest {

    @Test
    void waiting은_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new WaitingWithRank(null, 1L))
                .isInstanceOf(NullPointerException.class);
    }
} 

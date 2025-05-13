package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTimeTest {

    @Test
    void 예약시간은_NULL_일_수_없다() {
        assertThatThrownBy(() -> new ReservationTime(1L, null)).isInstanceOf(IllegalArgumentException.class);
    }
}


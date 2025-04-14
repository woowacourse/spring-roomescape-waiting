package roomescape.reservation.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.reservation.domain.exception.ReservationDateNullException;

public class ReservationDateTest {

    @Test
    void 날짜는_null이_될_수_없다() {
        assertThatThrownBy(() -> new ReservationDate(null))
                .isInstanceOf(ReservationDateNullException.class);
    }
}

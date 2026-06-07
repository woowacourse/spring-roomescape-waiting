package roomescape.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.custom.InvalidDomainValueException;

public class ReservationTimeTest {

    @Test
    void timeNullExceptionTest() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(InvalidDomainValueException.class);
    }

    @Test
    void checkPastTest() {
        LocalTime pastTime = LocalTime.of(7, 0);
        LocalTime futureTime = LocalTime.of(15, 0);

        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 0));

        assertThat(reservationTime.isPast(futureTime)).isTrue();
        assertThat(reservationTime.isPast(pastTime)).isFalse();
    }
}

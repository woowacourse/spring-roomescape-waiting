package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.domain.exception.DomainErrorCode;

public class ReservationTimeTest {

    @Test
    void timeNullExceptionTest() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code()).isEqualTo(DomainErrorCode.INVALID_INPUT));
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

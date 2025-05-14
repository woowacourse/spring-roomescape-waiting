package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.reservation.exception.InvalidReservationException;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    private final Theme theme = TestFixture.makeTheme(1L);

    @Test
    void createReservation_shouldThrowException_whenTimeIsBeforeNow() {
        assertThatThrownBy(() -> Reservation.createUpcomingReservationWithUnassignedId(
                TestFixture.makeMember(),
                LocalDate.now().minusDays(1),
                ReservationTime.of(1L, LocalTime.now().minusHours(1)),
                theme, LocalDateTime.now())
        ).isInstanceOf(InvalidReservationException.class)
                .hasMessageContaining("예약 시간이 현재 시간보다 이전일 수 없습니다.");
    }
}

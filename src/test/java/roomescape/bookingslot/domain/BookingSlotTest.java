package roomescape.bookingslot.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.bookingslot.exception.InvalidReservationException;
import roomescape.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class BookingSlotTest {

    private final Theme theme = TestFixture.makeTheme();

    @Test
    void createReservation_shouldThrowException_whenTimeIsBeforeNow() {
        assertThatThrownBy(() -> BookingSlot.createUpcomingReservation(
                TestFixture.makeMember(),
                LocalDate.now().minusDays(1),
                ReservationTime.withUnassignedId( LocalTime.now().minusHours(1)),
                theme, LocalDateTime.now())
        ).isInstanceOf(InvalidReservationException.class)
                .hasMessageContaining("예약 시간이 현재 시간보다 이전일 수 없습니다.");
    }
}

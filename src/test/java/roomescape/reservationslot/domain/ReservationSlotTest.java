package roomescape.reservationslot.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.TestFixture.NOW_DATETIME;
import static roomescape.fixture.TestFixture.makeMember;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.fixture.TestFixture;
import roomescape.reservationslot.exception.InvalidReservationSlotException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationSlotTest {

    private final Theme theme = TestFixture.makeTheme();

    @Test
    void createReservation_shouldThrowException_whenTimeIsBeforeNow() {
        LocalDate date = LocalDate.now().minusDays(1);
        ReservationTime reservationTime = ReservationTime.withUnassignedId(LocalTime.now().minusHours(1));
        ReservationSlot reservationSlot = new ReservationSlot(date, reservationTime, theme);
        assertThatThrownBy(() -> reservationSlot.addReservation(makeMember(), NOW_DATETIME))
                .isInstanceOf(InvalidReservationSlotException.class)
                .hasMessageContaining("예약 시간이 현재 시간보다 이전일 수 없습니다.");
    }
}

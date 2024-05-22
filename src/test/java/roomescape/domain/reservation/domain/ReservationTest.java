package roomescape.domain.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.reservation.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.domain.reservation.ReservationStatus.WAITING;
import static roomescape.fixture.LocalDateFixture.TODAY;
import static roomescape.fixture.MemberFixture.MEMBER_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.TEN_RESERVATION_TIME;
import static roomescape.fixture.ThemeFixture.DUMMY_THEME;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.domain.reservation.Reservation;

public class ReservationTest {

    @DisplayName("status를 RESERVED로 변경합니다.")
    @Test
    void should_change_status_to_reserved() {
        Reservation waitingReservation = new Reservation(1L, TODAY, TEN_RESERVATION_TIME, DUMMY_THEME, MEMBER_MEMBER,
                WAITING,
                LocalDateTime.now());

        Reservation changedReservation = waitingReservation.changeStatusToReserved();

        assertThat(changedReservation.getStatus()).isEqualTo(RESERVED);
    }
}

package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.InitialMemberFixture.MEMBER_1;
import static roomescape.InitialReservationFixture.RESERVATION_1;
import static roomescape.InitialReservationFixture.RESERVATION_2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    @DisplayName("Reservation 객체의 동등성을 따질 때는 id만 확인한다.")
    void testEquals() {
        Reservation reservation = new Reservation(
                RESERVATION_1.getId(),
                RESERVATION_2.getDate(),
                RESERVATION_2.getReservationTime(),
                RESERVATION_2.getTheme(),
                MEMBER_1
        );

        assertThat(RESERVATION_1).isEqualTo(reservation);
    }
}

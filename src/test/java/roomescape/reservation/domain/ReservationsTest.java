package roomescape.reservation.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.fixture.ReservationFixture.SAVED_RESERVATION_1;
import static roomescape.reservation.fixture.ReservationFixture.SAVED_RESERVATION_2;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationsTest {

    @DisplayName("예약들 중 특정 멤버 아이디로 된 예약이 있는지 찾을 수 있다")
    @Test
    void should_check_reservations_has_reservation_made_by_specific_member() {
        Reservations reservations = new Reservations(List.of(SAVED_RESERVATION_1, SAVED_RESERVATION_2));

        assertAll(
                () -> assertThat(reservations.hasReservationMadeBy(1L)).isTrue(),
                () -> assertThat(reservations.hasReservationMadeBy(100L)).isFalse()
        );
    }
}

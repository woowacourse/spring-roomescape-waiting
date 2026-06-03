package roomescape.reservation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReservationTest {

    @Test
    @DisplayName("예약자가 같으면 본인 예약이다.")
    void 예약_테스트_1() {
        Reservation reservation = Reservation.of(1L, 1L, 1L);

        assertThat(reservation.isOwnedBy(1L)).isTrue();
    }

    @Test
    @DisplayName("예약자가 다르면 본인 예약이 아니다.")
    void 예약_테스트_2() {
        Reservation reservation = Reservation.of(1L, 1L, 1L);

        assertThat(reservation.isOwnedBy(2L)).isFalse();
    }
}

package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void PK_와_함께_생성된다() {
        // given
        Long primaryKey = 1L;
        Reservation reservationWithoutPrimaryKey = ReservationFixture.createWithoutId();

        // when
        Reservation reservation = Reservation.createWithPrimaryKey(reservationWithoutPrimaryKey, primaryKey);

        // then
        assertThat(reservationWithoutPrimaryKey.getId()).isNull();
        assertEquals(primaryKey, reservation.getId());
    }
}

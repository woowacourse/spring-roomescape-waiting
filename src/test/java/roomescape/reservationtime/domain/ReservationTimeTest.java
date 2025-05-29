package roomescape.reservationtime.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    void PK와_함께_생성된다() {

        // given
        ReservationTime reservationTimeWithoutId = ReservationTimeFixture.createWithoutId();
        Long primaryKey = 1L;

        // when
        ReservationTime reservationTime = ReservationTime.createWithPrimaryKey(reservationTimeWithoutId, primaryKey);

        // then
        assertAll(
            () -> assertEquals(primaryKey, reservationTime.getId()),
            () -> assertEquals(reservationTimeWithoutId.getStartAt(), reservationTime.getStartAt())
        );
    }
}

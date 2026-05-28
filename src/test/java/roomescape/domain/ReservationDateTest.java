package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class ReservationDateTest {

    @Test
    void 예약날짜_과거인_경우_성공() {
        // given
        LocalDate past = LocalDate.now().minusDays(1);

        // when
        ReservationDate reservationDate = new ReservationDate(past);

        // then
        Assertions.assertThat(reservationDate.isPast()).isTrue();
    }

    @Test
    void 예약날짜_미래인_경우_실패() {
        // given
        LocalDate future = LocalDate.now().plusDays(1);

        // when
        ReservationDate reservationDate = new ReservationDate(future);

        // then
        Assertions.assertThat(reservationDate.isPast()).isFalse();
    }

    @Test
    void 예약날짜_오늘인_경우_실패() {
        // given
        LocalDate future = LocalDate.now();

        // when
        ReservationDate reservationDate = new ReservationDate(future);

        // then
        Assertions.assertThat(reservationDate.isPast()).isFalse();
    }
}

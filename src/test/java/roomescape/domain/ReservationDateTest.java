package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class ReservationDateTest {

    @Test
    void isPast_과거인_경우_성공() {
        // given
        LocalDate past = LocalDate.now().minusDays(1);

        // when
        ReservationDate reservationDate = new ReservationDate(past);

        // then
        Assertions.assertThat(reservationDate.isPast()).isTrue();
    }

    @Test
    void isPast_미래인_경우_실패() {
        // given
        LocalDate future = LocalDate.now().plusDays(1);

        // when
        ReservationDate reservationDate = new ReservationDate(future);

        // then
        Assertions.assertThat(reservationDate.isPast()).isFalse();
    }

    @Test
    void isPast_오늘인_경우_실패() {
        // given
        LocalDate future = LocalDate.now();

        // when
        ReservationDate reservationDate = new ReservationDate(future);

        // then
        Assertions.assertThat(reservationDate.isPast()).isFalse();
    }

    @Test
    void isToday_과거인_경우_실패() {
        // given
        LocalDate past = LocalDate.now().minusDays(1);

        // when
        ReservationDate reservationDate = new ReservationDate(past);

        // then
        Assertions.assertThat(reservationDate.isToday()).isFalse();
    }

    @Test
    void isToday_미래인_경우_실패() {
        // given
        LocalDate future = LocalDate.now().plusDays(1);

        // when
        ReservationDate reservationDate = new ReservationDate(future);

        // then
        Assertions.assertThat(reservationDate.isToday()).isFalse();
    }

    @Test
    void isToday_오늘인_경우_성공() {
        // given
        LocalDate future = LocalDate.now();

        // when
        ReservationDate reservationDate = new ReservationDate(future);

        // then
        Assertions.assertThat(reservationDate.isToday()).isTrue();
    }
}

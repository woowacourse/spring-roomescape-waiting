package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;

class ReservationDateTest {

    @Test
    void 생성_성공() {
        // given
        LocalDate date = LocalDate.now();

        // when
        ReservationDate reservationDate = new ReservationDate(date);

        // then
        Assertions.assertThat(reservationDate.getDate()).isEqualTo(date);
    }

    @Test
    void 생성_null인_경우_실패() {
        // given
        LocalDate date = null;

        // when && then
        Assertions.assertThatThrownBy(() -> new ReservationDate(date))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_NULL);
    }

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

    @Test
    void getReservableDates_오늘부터_14일_반환() {
        // when
        List<LocalDate> dates = ReservationDate.getReservableDates();

        // then
        Assertions.assertThat(dates).hasSize(14);
        Assertions.assertThat(dates.getFirst()).isEqualTo(LocalDate.now());
        Assertions.assertThat(dates.getLast()).isEqualTo(LocalDate.now().plusDays(13));
    }
}

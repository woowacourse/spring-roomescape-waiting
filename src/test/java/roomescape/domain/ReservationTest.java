package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private final ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
    private final Theme theme = Theme.createWithId(1L, "우테코", "우테코는 재밌어", "https://wooteco.com/thumbnail.jpg", 30000L);

    @Test
    void Reservation_객체_생성() {
        final String name = "재즈";
        final LocalDate date = LocalDate.now();

        final Reservation reservation = Reservation.create(name, date, reservationTime, theme);

        assertThat(reservation.getName()).isEqualTo(name);
        assertThat(reservation.getDate()).isEqualTo(date);
        assertThat(reservation.getTime()).isEqualTo(reservationTime);
        assertThat(reservation.getTheme()).isEqualTo(theme);
    }

    @Test
    void 날짜가_null이면_예외발생() {
        assertThatThrownBy(() -> Reservation.create("재즈", null, reservationTime, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ID를_포함한_Reservation_객체_생성() {
        final Reservation reservation = Reservation.createWithId(1L, "재즈", LocalDate.now(), reservationTime, theme);

        assertThat(reservation.getId()).isEqualTo(1L);
    }

    @Test
    void ID가_null이면_예외발생() {
        assertThatThrownBy(() -> Reservation.createWithId(null, "재즈", LocalDate.now(), reservationTime, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ID를_변경한_새로운_Reservation_객체_반환() {
        final Reservation reservation = Reservation.create("재즈", LocalDate.now(), reservationTime, theme);

        final Reservation reservationWithId = reservation.withId(1L);

        assertThat(reservationWithId.getId()).isEqualTo(1L);
        assertThat(reservationWithId.getName()).isEqualTo(reservation.getName());
    }

    @Test
    void 예약의_날짜와_시간을_변경() {
        final Reservation reservation = Reservation.create("재즈", LocalDate.now(), reservationTime, theme);
        final LocalDate newDate = LocalDate.now().plusDays(1);
        final ReservationTime newTime = ReservationTime.createWithId(2L, LocalTime.of(12, 0), LocalTime.of(13, 0));

        final Reservation modifiedReservation = reservation.modify(newDate, newTime);

        assertThat(modifiedReservation.getDate()).isEqualTo(newDate);
        assertThat(modifiedReservation.getTime()).isEqualTo(newTime);
        assertThat(modifiedReservation.getName()).isEqualTo(reservation.getName());
    }
}

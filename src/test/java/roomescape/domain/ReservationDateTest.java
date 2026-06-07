package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationDateTest {

    @Test
    void ReservationDate_객체_생성() {
        final LocalDate date = LocalDate.now();

        final ReservationDate reservationDate = new ReservationDate(date);

        assertThat(reservationDate.date()).isEqualTo(date);
    }

    @Test
    void 날짜가_null이면_예외발생() {
        assertThatThrownBy(() -> new ReservationDate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 오늘_날짜인지_확인() {
        final ReservationDate today = new ReservationDate(LocalDate.now());
        final ReservationDate tomorrow = new ReservationDate(LocalDate.now().plusDays(1));

        assertThat(today.isToday()).isTrue();
        assertThat(tomorrow.isToday()).isFalse();
    }

    @Test
    void 과거_날짜인지_확인() {
        final ReservationDate yesterday = new ReservationDate(LocalDate.now().minusDays(1));
        final ReservationDate today = new ReservationDate(LocalDate.now());

        assertThat(yesterday.isPast()).isTrue();
        assertThat(today.isPast()).isFalse();
    }
}

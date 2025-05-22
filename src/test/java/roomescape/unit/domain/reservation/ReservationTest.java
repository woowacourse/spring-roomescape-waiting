package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTest {

    @Test
    void 날짜가_범위_사이에_있는지_확인할_수_있다() {
        LocalDate today = LocalDate.now();
        Reservation reservation = new Reservation(null, "tuda", today,
                new ReservationTime(null, LocalTime.now().plusHours(1L)), null);

        LocalDate from = today.minusDays(1);
        LocalDate to = today.plusDays(1);

        boolean result = reservation.isBetweenDate(from, to);

        assertThat(result).isTrue();
    }

    @Test
    void 날짜가_시작_범위보다_이전인지_확인할_수_있다() {
        LocalDate today = LocalDate.now();

        Reservation reservation = new Reservation(
                null,
                "tuda",
                today.minusDays(2),
                new ReservationTime(null, LocalTime.now().plusHours(1L)),
                null
        );

        LocalDate from = today.minusDays(1);
        LocalDate to = today.plusDays(1);

        boolean result = reservation.isBetweenDate(from, to);

        assertThat(result).isFalse();
    }

    @Test
    void 날짜가_종료_범위보다_이후인지_확인할_수_있다() {
        LocalDate today = LocalDate.now();

        Reservation reservation = new Reservation(
                null,
                "tuda",
                today.plusDays(2),
                new ReservationTime(null, LocalTime.now().plusHours(1L)),
                null
        );

        LocalDate from = today.minusDays(1);
        LocalDate to = today.plusDays(1);

        boolean result = reservation.isBetweenDate(from, to);

        assertThat(result).isFalse();
    }
}


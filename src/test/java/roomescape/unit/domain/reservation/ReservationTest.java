package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTest {

    @Test
    void 예약은_공백이거나_NULL_로_이루어질_수_없다() {
        assertThatThrownBy(
                () -> new Reservation(1L, "", LocalDate.now(), new ReservationTime(1L, LocalTime.now()),
                        new Theme(1L, "테마", "설명", "썸네일")))
                .isInstanceOf(IllegalArgumentException.class);
    }

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


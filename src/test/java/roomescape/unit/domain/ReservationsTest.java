package roomescape.unit.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Reservations;
import roomescape.domain.Theme;
import roomescape.exception.BusinessRuleViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationsTest {

    private static final Theme THEME = new Theme(
            1L,
            "공포",
            "무서운 테마",
            "https://example.com/img.jpg"
    );
    private static final ReservationTime TIME_10 = new ReservationTime(1L, LocalTime.of(10, 0));
    private static final ReservationTime TIME_11 = new ReservationTime(2L, LocalTime.of(11, 0));

    @Test
    void 예약된_시간은_occupied_true를_반환한다() {
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 8, 5),
                TIME_10,
                THEME
        );
        Reservations reservations = new Reservations(List.of(reservation));

        assertThat(reservations.isOccupied(TIME_10)).isTrue();
    }

    @Test
    void 예약되지_않은_시간은_occupied_false를_반환한다() {
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 8, 5),
                TIME_10,
                THEME
        );
        Reservations reservations = new Reservations(List.of(reservation));

        assertThat(reservations.isOccupied(TIME_11)).isFalse();
    }

    @Test
    void 예약이_없으면_모든_시간은_occupied_false를_반환한다() {
        Reservations reservations = new Reservations(List.of());

        assertThat(reservations.isOccupied(TIME_10)).isFalse();
    }

    @Test
    void findByTime은_해당_시간의_예약을_반환한다() {
        Reservation reservation = new Reservation(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                TIME_10,
                THEME
        );
        Reservations reservations = new Reservations(List.of(reservation));

        assertThat(reservations.findByTime(TIME_10)).isEqualTo(reservation);
    }

    @Test
    void findByTime은_예약되지_않은_시간이면_예외를_던진다() {
        Reservations reservations = new Reservations(List.of());

        assertThatThrownBy(() -> reservations.findByTime(TIME_10))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void excluding은_지정한_id의_예약을_제외한다() {
        Reservation kept = new Reservation(
                1L,
                "브라운",
                LocalDate.of(2026, 8, 5),
                TIME_10,
                THEME
        );
        Reservation removed = new Reservation(
                2L,
                "티뉴",
                LocalDate.of(2026, 8, 5),
                TIME_11,
                THEME
        );
        Reservations reservations = new Reservations(List.of(kept, removed));

        Reservations result = reservations.excluding(2L);

        assertThat(result.isOccupied(TIME_10)).isTrue();
        assertThat(result.isOccupied(TIME_11)).isFalse();
    }
}

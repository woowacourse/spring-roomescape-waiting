package roomescape.unit.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    private static final Theme ANY_THEME = new Theme(
            1L,
            "공포",
            "설명",
            "https://example.com/horror.jpg"
    );

    @Test
    void 예약_시점이_현재보다_과거면_isPast가_true를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 5, 14),
                time,
                ANY_THEME
        );

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isTrue();
    }

    @Test
    void 예약_시점이_현재보다_미래면_isPast가_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(15, 0));
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 5, 14),
                time,
                ANY_THEME
        );

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    void 예약_시점과_현재가_정확히_같으면_isPast가_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 30));
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 5, 14),
                time,
                ANY_THEME
        );

        LocalDateTime now = LocalDateTime.of(2026, 5, 14, 14, 30);

        assertThat(reservation.isPast(now)).isFalse();
    }

    @Test
    void 예약자_이름과_같으면_isOwnedBy가_true를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 5, 14),
                time,
                ANY_THEME
        );

        assertThat(reservation.isOwnedBy("브라운")).isTrue();
    }

    @Test
    void 예약자_이름과_다르면_isOwnedBy가_false를_반환한다() {
        ReservationTime time = new ReservationTime(1L, LocalTime.of(14, 0));
        Reservation reservation = new Reservation(
                "브라운",
                LocalDate.of(2026, 5, 14),
                time,
                ANY_THEME
        );

        assertThat(reservation.isOwnedBy("티뉴")).isFalse();
    }
}

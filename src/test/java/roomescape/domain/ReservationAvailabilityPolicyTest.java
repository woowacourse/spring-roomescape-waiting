package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationAvailabilityPolicy;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

class ReservationAvailabilityPolicyTest {
    private final ReservationAvailabilityPolicy reservationAvailabilityPolicy = new ReservationAvailabilityPolicy();

    @Test
    @DisplayName("과거 날짜와 시간으로는 예약할 수 없다")
    void validatePastDateTime_ThrowsException() {
        LocalDate date = LocalDate.parse("2026-03-08");
        ReservationTime time = ReservationTime.createNew(LocalTime.parse("10:00"));
        LocalDateTime standardDateTime = LocalDateTime.parse("2026-03-08T10:01:00");

        assertThatThrownBy(() -> reservationAvailabilityPolicy.validateReservable(date, time, standardDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationAvailabilityPolicy.PAST_RESERVATION_MESSAGE);
    }

    @Test
    @DisplayName("예약 시각이 기준 시각보다 이전이면 과거 예약이다")
    void isPastReservation() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        boolean past = reservationAvailabilityPolicy.isPast(
                reservation,
                LocalDateTime.parse("2026-08-06T10:01:00")
        );

        assertThat(past).isTrue();
    }

    @Test
    @DisplayName("지난 예약에는 대기를 생성할 수 없다")
    void validatePastWaiting_ThrowsException() {
        Reservation reservation = createReservation(LocalDate.parse("2026-08-06"), LocalTime.parse("10:00"));

        assertThatThrownBy(() -> reservationAvailabilityPolicy.validateWaitable(
                reservation,
                LocalDateTime.parse("2026-08-06T10:01:00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ReservationAvailabilityPolicy.PAST_WAITING_MESSAGE);
    }

    private Reservation createReservation(final LocalDate date, final LocalTime time) {
        Theme theme = Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
        ReservationTime reservationTime = ReservationTime.of(1L, time);

        return Reservation.of(1L, "쿠다", date, theme, reservationTime);
    }
}

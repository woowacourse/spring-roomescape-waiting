package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.ConflictException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 8, 10, 30);
    private static final ReservationTime PAST_TIME = ReservationTime.of(1L, LocalTime.of(10, 0));
    private static final ReservationTime FUTURE_TIME = ReservationTime.of(2L, LocalTime.of(11, 0));
    private static final Theme THEME = Theme.of(1L, "링", "공포 테마", "http:~");

    @Test
    @DisplayName("현재 이전 시간으로 예약을 생성할 수 없다")
    void cannotCreateReservationBeforeNow() {
        assertThatThrownBy(() -> Reservation.create(
                "브라운",
                "customer@example.com",
                LocalDate.of(2026, 5, 8),
                PAST_TIME,
                THEME,
                NOW
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("현재 이후 시간으로 예약을 생성한다")
    void createReservationAfterNow() {
        Reservation reservation = Reservation.create(
                "브라운",
                "customer@example.com",
                LocalDate.of(2026, 5, 8),
                FUTURE_TIME,
                THEME,
                NOW
        );

        assertThat(reservation.getCustomerName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("과거 예약도 복원할 수 있다")
    void restorePastReservation() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                LocalDate.of(2026, 5, 8),
                PAST_TIME,
                THEME
        );

        assertThat(reservation.getDate()).isEqualTo(LocalDate.of(2026, 5, 8));
    }

    @Test
    @DisplayName("예약 일정을 변경한다")
    void changeReservationSchedule() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                LocalDate.of(2026, 5, 8),
                FUTURE_TIME,
                THEME
        );
        ReservationTime newTime = ReservationTime.of(3L, LocalTime.of(12, 0));

        Reservation changed = reservation.changeSchedule(
                LocalDate.of(2026, 5, 9),
                newTime,
                NOW
        );

        assertThat(changed.getId()).isEqualTo(1L);
        assertThat(changed.getCustomerName()).isEqualTo("브라운");
        assertThat(changed.getDate()).isEqualTo(LocalDate.of(2026, 5, 9));
        assertThat(changed.getTime()).isEqualTo(newTime);
        assertThat(changed.getTheme()).isEqualTo(THEME);
    }

    @Test
    @DisplayName("과거 시간으로 예약 일정을 변경할 수 없다")
    void cannotChangeReservationScheduleToPastTime() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                LocalDate.of(2026, 5, 9),
                FUTURE_TIME,
                THEME
        );

        assertThatThrownBy(() -> reservation.changeSchedule(
                LocalDate.of(2026, 5, 8),
                PAST_TIME,
                NOW
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약일 당일에는 예약 시작 전이어도 사용자가 예약을 취소할 수 없다")
    void customerCannotCancelReservationOnReservationDateBeforeStartTime() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                LocalDate.of(2026, 5, 8),
                FUTURE_TIME,
                THEME
        );

        assertThatThrownBy(() -> reservation.validateCancelableByCustomer(LocalDate.of(2026, 5, 8)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("당일 예약은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("예약일 하루 전에는 사용자가 예약을 취소할 수 있다")
    void customerCanCancelReservationOneDayBeforeReservationDate() {
        Reservation reservation = Reservation.of(
                1L,
                "브라운",
                "customer@example.com",
                LocalDate.of(2026, 5, 9),
                FUTURE_TIME,
                THEME
        );

        assertThatCode(() -> reservation.validateCancelableByCustomer(LocalDate.of(2026, 5, 8)))
                .doesNotThrowAnyException();
    }
}

package roomescape.reservation.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.Theme;

public class ReservationTest {

    @Test
    @DisplayName("예약자가 같으면 본인 예약이다.")
    void 예약_테스트_1() {
        Reservation reservation = Reservation.of(1L, 1L, slot());

        assertThat(reservation.isOwnedBy(1L)).isTrue();
    }

    @Test
    @DisplayName("예약자가 다르면 본인 예약이 아니다.")
    void 예약_테스트_2() {
        Reservation reservation = Reservation.of(1L, 1L, slot());

        assertThat(reservation.isOwnedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("예약자가 다르면 소유자 검증에 실패한다.")
    void validateOwnedBy_fail() {
        Reservation reservation = Reservation.of(1L, 1L, slot());

        assertThatThrownBy(() -> reservation.validateOwnedBy(2L))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("예약 시간이 과거이면 검증에 실패한다.")
    void validateNotPast_fail() {
        Reservation reservation = Reservation.of(1L, 1L, slot());

        assertThatThrownBy(() -> reservation.validateNotPast(LocalDateTime.of(2026, 5, 5, 10, 1)))
                .isInstanceOf(EscapeRoomException.class);
    }

    private Slot slot() {
        return Slot.of(
                1L,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }
}

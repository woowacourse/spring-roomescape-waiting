package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.domain.Slot;
import roomescape.theme.domain.Theme;

public class ReservationTest {

    @Test
    @DisplayName("예약자가 같으면 본인 예약이다.")
    void same_member_is_owner_of_reservation() {
        Reservation reservation = roomescape.TestFixtures.reservation(1L, 1L, slot());

        assertThat(reservation.isOwnedBy(1L)).isTrue();
    }

    private Slot slot() {
        return Slot.of(
                1L,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "theme", "description", "thumbnail")
        );
    }

    @Test
    @DisplayName("예약자가 다르면 본인 예약이 아니다.")
    void different_member_is_not_owner_of_reservation() {
        Reservation reservation = roomescape.TestFixtures.reservation(1L, 1L, slot());

        assertThat(reservation.isOwnedBy(2L)).isFalse();
    }

    @Test
    @DisplayName("예약자가 다르면 소유자 검증에 실패한다.")
    void different_member_fails_reservation_owner_validation() {
        Reservation reservation = roomescape.TestFixtures.reservation(1L, 1L, slot());

        assertThatThrownBy(() -> reservation.validateOwnedBy(2L))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("예약 시간이 과거이면 검증에 실패한다.")
    void past_reservation_time_fails_validation() {
        Reservation reservation = roomescape.TestFixtures.reservation(1L, 1L, slot());

        assertThatThrownBy(() -> reservation.validateNotPast(LocalDateTime.of(2026, 5, 5, 10, 1)))
                .isInstanceOf(EscapeRoomException.class);
    }

}

package roomescape.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixtures.anyUserWithNewId;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixtures;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;

class UserTest {

    private final LocalDate date = LocalDate.of(2020, 1, 1);
    private final TimeSlot timeSlot = TestFixtures.anyTimeSlotWithNewId();
    private final Theme theme = TestFixtures.anyThemeWithNewId();
    private final ReservationSlot slot = ReservationSlot.of(date, timeSlot, theme);

    @Test
    @DisplayName("사용자가 ADMIN인 지 알 수 있다.")
    void isAdmin() {
        var adminUser = new User(1L, new UserName("어드민"), UserRole.ADMIN, new Email("admin@email.com"), new Password("password"));
        var notAdminUser = new User(2L, new UserName("유저"), UserRole.USER, new Email("popo@email.com"), new Password("password"));

        assertAll(
            () -> assertThat(adminUser.isAdmin()).isTrue(),
            () -> assertThat(notAdminUser.isAdmin()).isFalse()
        );
    }

    @Test
    @DisplayName("예약을 할 수 있다.")
    void reserve() {
        // given
        var user = anyUserWithNewId();
        var reservation = new Reservation(1L, user, slot, ReservationStatus.RESERVED);

        // when
        user.reserve(reservation);

        // then
        assertThat(user.reservations()).contains(reservation);
    }

    @Test
    @DisplayName("이미 예약한 슬롯에는 예약을 할 수 없다.")
    void reserveDuplicateSlot() {
        // given
        var user = anyUserWithNewId();
        var reservation = new Reservation(1L, user, slot, ReservationStatus.RESERVED);
        user.reserve(reservation);

        var reservationWithDuplicatedSlot = new Reservation(2L, user, slot, ReservationStatus.WAITING);

        // when & then
        assertThatThrownBy(() -> user.reserve(reservationWithDuplicatedSlot))
            .isInstanceOf(AlreadyExistedException.class);
    }

    @Test
    @DisplayName("예약을 취소할 수 있다.")
    void cancelReservation() {
        // given
        var user = anyUserWithNewId();
        var reservation = new Reservation(1L, user, slot, ReservationStatus.WAITING);
        user.reserve(reservation);

        // when
        user.cancelReservation(reservation);

        // then
        assertThat(user.reservations()).doesNotContain(reservation);
    }

    @Test
    @DisplayName("사용자가 가진 예약만 취소할 수 있다.")
    void cancelReservationCanOnlyMine() {
        // given
        var user = anyUserWithNewId();
        user.reserve(new Reservation(1L, user, slot, ReservationStatus.RESERVED));

        var othersReservation = new Reservation(2L, anyUserWithNewId(), slot, ReservationStatus.RESERVED);

        // when & then
        assertThatThrownBy(() -> user.cancelReservation(othersReservation))
            .isInstanceOf(NotFoundException.class);
    }
}

package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.TestFixtures;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;
import roomescape.exception.BusinessRuleViolationException;

public class ReservationTest {

    private final User user1 = TestFixtures.anyUserWithNewId();
    private final User user2 = TestFixtures.anyUserWithNewId();
    private final LocalDate date = LocalDate.of(2020, 1, 1);
    private final TimeSlot timeSlot = TestFixtures.anyTimeSlotWithNewId();
    private final Theme theme = TestFixtures.anyThemeWithNewId();

    @Test
    @DisplayName("예약자를 비교할 수 있다.")
    void isOwnedBy() {
        // given
        var reservation = new Reservation(
            1L,
            user1,
            ReservationSlot.of(date, timeSlot, theme),
            ReservationStatus.RESERVED
        );

        // when
        var ownedByUser1 = reservation.isOwnedBy(user1);
        var ownedByUser2 = reservation.isOwnedBy(user2);

        // then
        assertAll(
            () -> assertThat(ownedByUser1).isTrue(),
            () -> assertThat(ownedByUser2).isFalse()
        );
    }

    @Test
    @DisplayName("대기 상태의 예약을 취소할 수 있다.")
    void cancel() {
        // given
        var reservation = reservationOf(ReservationStatus.WAITING);

        // when
        reservation.cancel();

        // then
        assertThat(reservation.status()).isEqualTo(ReservationStatus.CANCELED);
    }

    @ParameterizedTest
    @CsvSource({"RESERVED", "CANCELED"})
    @DisplayName("대기 상태가 아닌 예약을 취소하려 하면 예외가 발생한다.")
    void cancelNotWaitingReservation(ReservationStatus statusThatNotWaiting) {
        var reservation = reservationOf(statusThatNotWaiting);

        assertThatThrownBy(reservation::cancel).isInstanceOf(BusinessRuleViolationException.class);
    }

    private Reservation reservationOf(final ReservationStatus status) {
        return new Reservation(1L, user1, ReservationSlot.of(date, timeSlot, theme), status);
    }
}

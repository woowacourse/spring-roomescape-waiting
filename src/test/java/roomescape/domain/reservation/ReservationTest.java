package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixtures;

public class ReservationTest {

    @Test
    @DisplayName("예약자를 비교할 수 있다.")
    void isOwnedBy() {
        // given
        var user1 = TestFixtures.anyUserWithNewId();
        var user2 = TestFixtures.anyUserWithNewId();

        var date = LocalDate.of(2020, 1, 1);
        var timeSlot = TestFixtures.anyTimeSlotWithNewId();
        var theme = TestFixtures.anyThemeWithNewId();

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
}

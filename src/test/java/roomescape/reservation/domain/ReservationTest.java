package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.HORROR_THEME;
import static roomescape.util.Fixture.KAKI;
import static roomescape.util.Fixture.RESERVATION_HOUR_10;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @DisplayName("현재 날짜보다 이전 날짜로 예약시 예외가 발생한다.")
    @Test
    void createReservationByLastDate() {
        Theme horrorTheme = HORROR_THEME;

        ReservationTime hour10 = RESERVATION_HOUR_10;

        assertThatThrownBy(() -> new Reservation(
                        KAKI,
                        LocalDate.now().minusDays(1),
                        horrorTheme,
                        hour10,
                        ReservationStatus.SUCCESS
                )
        ).isInstanceOf(IllegalArgumentException.class);
    }
}

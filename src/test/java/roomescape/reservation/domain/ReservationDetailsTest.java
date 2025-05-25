package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationDetailsTest {

    @DisplayName("Date가 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationDateTest() {
        // given
        ReservationTime time = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "우테코", "방탈출", "https://");

        // when & then
        assertThatThrownBy(() -> new ReservationDetails(null, time, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("ReservationTime이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationTimeTest() {
        // given
        LocalDate date = LocalDate.now();
        Theme theme = new Theme(1L, "우테코", "방탈출", "https://");

        // when & then
        assertThatThrownBy(() -> new ReservationDetails(date, null, theme))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Theme이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidThemeTest() {
        // given
        LocalDate date = LocalDate.now();
        ReservationTime time = new ReservationTime(1L, LocalTime.now());

        // when & then
        assertThatThrownBy(() -> new ReservationDetails(date, time, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.support.exception.RoomescapeException;

class ReservationDateTest {

    @Test
    @DisplayName("예약 날짜를 생성한다.")
    void createReservationDate() {
        LocalDate playDay = LocalDate.now();
        ReservationDate reservationDate = ReservationDate.createWithoutId(playDay);

        assertThat(reservationDate.getPlayDay()).isEqualTo(playDay);
    }

    @Test
    @DisplayName("날짜가 null이면 예외가 발생한다.")
    void createWithNullDate() {
        assertThatThrownBy(() -> ReservationDate.createWithoutId(null))
            .isInstanceOf(RoomescapeException.class);
    }

    @Test
    @DisplayName("오늘 또는 미래의 날짜인지 확인한다.")
    void isAvailable() {
        LocalDate today = LocalDate.now();
        ReservationDate pastDate = ReservationDate.createWithoutId(today.minusDays(1));
        ReservationDate todayDate = ReservationDate.createWithoutId(today);
        ReservationDate futureDate = ReservationDate.createWithoutId(today.plusDays(1));

        assertThat(pastDate.isAvailable(today)).isFalse();
        assertThat(todayDate.isAvailable(today)).isTrue();
        assertThat(futureDate.isAvailable(today)).isTrue();
    }

    @Test
    @DisplayName("과거인지 확인한다.")
    void isPast() {
        LocalDate today = LocalDate.now();
        ReservationDate pastDate = ReservationDate.createWithoutId(today.minusDays(1));
        ReservationDate futureDate = ReservationDate.createWithoutId(today.plusDays(1));
        LocalTime time = LocalTime.now().minusSeconds(1);
        ReservationTime reservationTime = ReservationTime.createWithoutId(time);

        assertThat(pastDate.isPast(reservationTime)).isTrue();
        assertThat(futureDate.isPast(reservationTime)).isFalse();
    }

    @Test
    @DisplayName("오늘인지 확인한다.")
    void isToday() {
        LocalDate today = LocalDate.now();
        ReservationDate todayDate = ReservationDate.createWithoutId(today);
        ReservationDate notTodayDate = ReservationDate.createWithoutId(today.minusDays(1));

        assertThat(todayDate.isToday()).isTrue();
        assertThat(notTodayDate.isToday()).isFalse();
    }
}

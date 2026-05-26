package roomescape.domain.reservationdate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
}

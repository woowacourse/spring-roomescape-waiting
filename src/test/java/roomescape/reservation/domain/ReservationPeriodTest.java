package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationPeriodTest {

    @Test
    @DisplayName("시작 날짜 계산 테스트")
    void find_start_date_test() {
        // given
        ReservationPeriod reservationPeriod = new ReservationPeriod(LocalDate.of(2000, 11, 5), 3, 1);
        // when
        LocalDate startDate = reservationPeriod.findStartDate();
        // then
        assertThat(startDate).isEqualTo(LocalDate.of(2000, 11, 2));
    }

    @Test
    @DisplayName("끝 날짜 계산 테스트")
    void find_end_date_test() {
        // given
        ReservationPeriod reservationPeriod = new ReservationPeriod(LocalDate.of(2000, 11, 5), 3, 1);
        // when
        LocalDate endDate = reservationPeriod.findEndDate();
        // then
        assertThat(endDate).isEqualTo(LocalDate.of(2000, 11, 4));
    }

    @Test
    @DisplayName("offset 음수 예외 테스트")
    void offset_negative_test() {
        assertThatThrownBy(() -> new ReservationPeriod(LocalDate.of(2000, 11, 5), -1, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("offset 범위 예외 테스트")
    void offset_range_test() {
        assertThatThrownBy(() -> new ReservationPeriod(LocalDate.of(2000, 11, 5), 1, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

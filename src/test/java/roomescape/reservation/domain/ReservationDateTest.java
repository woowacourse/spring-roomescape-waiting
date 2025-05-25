package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationDateTest {

    @DisplayName("과거 날짜인지 검사한다")
    @Test
    void isInPast() {
        // given
        ReservationDate yesterday = new ReservationDate(LocalDate.now().minusDays(1));
        ReservationDate today = new ReservationDate(LocalDate.now());
        ReservationDate tomorrow = new ReservationDate(LocalDate.now().plusDays(1));

        // when & then
        assertThat(yesterday.isInPast()).isTrue();
        assertThat(today.isInPast()).isFalse();
        assertThat(tomorrow.isInPast()).isFalse();
    }

    @DisplayName("오늘인지 확인한다")
    @Test
    void isToday() {
        // given
        ReservationDate yesterday = new ReservationDate(LocalDate.now().minusDays(1));
        ReservationDate today = new ReservationDate(LocalDate.now());
        ReservationDate tomorrow = new ReservationDate(LocalDate.now().plusDays(1));

        // when & then
        assertThat(yesterday.isToday()).isFalse();
        assertThat(today.isToday()).isTrue();
        assertThat(tomorrow.isToday()).isFalse();
    }
}
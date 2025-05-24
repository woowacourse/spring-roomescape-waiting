package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;

class ReservationDateTest {

    @DisplayName("주어진 날짜와 동일하다면 true를 반환한다.")
    @ParameterizedTest
    @CsvSource(value = {"2025-12-01:true", "2025-12-02:false"}, delimiter = ':')
    void isEqualToDate(final LocalDate date, final boolean expected) {
        // given
        final ReservationDate reservationDate = ReservationDate.fromQuery(LocalDate.of(2025, 12, 1));

        // when
        final boolean actual = reservationDate.isEqualToDate(date);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("주어진 날짜가 오늘이라면 정상적으로 생성된다.")
    @Test
    void of1() {
        // given
        final LocalDate today = LocalDate.of(2025, 12, 1);
        final LocalDate date = LocalDate.of(2025, 12, 1);
        // when
        final ReservationDate actual = ReservationDate.of(date, today);

        //
        assertThat(actual.date()).isEqualTo(LocalDate.of(2025, 12, 1));
    }

    @DisplayName("주어진 날짜가 미래 시점이라면 정상적으로 생성된다.")
    @Test
    void of2() {
        // given
        final LocalDate today = LocalDate.of(2025, 12, 1);
        final LocalDate date = LocalDate.of(2025, 12, 2);
        // when
        final ReservationDate actual = ReservationDate.of(date, today);

        //
        assertThat(actual.date()).isEqualTo(LocalDate.of(2025, 12, 2));
    }

    @DisplayName("주어진 날짜가 과거 날짜라면 예외를 던진다.")
    @Test
    void of3() {
        // given
        final LocalDate today = LocalDate.of(2025, 12, 1);
        final LocalDate pastDate = LocalDate.of(2025, 11, 30);
        // when & then
        assertThatThrownBy(()->{
            ReservationDate.of(pastDate, today);
        }).isInstanceOf(ReservationPastDateException.class);
    }

    @DisplayName("주어진 날짜로 ReservationDate를 생성한다.")
    @Test
    void fromQuery() {
        // given
        final LocalDate date = LocalDate.of(2025, 12, 1);

        // when
        final ReservationDate actual = ReservationDate.fromQuery(date);

        // then
        assertThat(actual.date()).isEqualTo(LocalDate.of(2025, 12, 1));
    }
}

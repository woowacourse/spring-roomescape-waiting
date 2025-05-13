package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.time.domain.ReservationTime;

class ReservationDateTimeTest {

    @DisplayName("주어진 날짜와 시간보다 이전인지 확인한다.")
    @ParameterizedTest
    @CsvSource(value = {"2025-04-29;09:59;False", "2025-04-29;10:00;True", "2025-04-29;10:01;True",
            "2025-04-28;10:01;False", "2025-04-30;10:01;True"}, delimiterString = ";")
    void testIsBefore(LocalDate date, LocalTime time, boolean expected) {
        // given
        LocalDate reservationDate = LocalDate.of(2025, 4, 29);
        LocalTime reservationTime = LocalTime.of(10, 0);
        ReservationDateTime dateTime = new ReservationDateTime(reservationDate,
                new ReservationTime(1L, reservationTime));
        // when
        boolean result = dateTime.isBefore(LocalDateTime.of(date, time));
        // then
        assertThat(result).isEqualTo(expected);
    }
}

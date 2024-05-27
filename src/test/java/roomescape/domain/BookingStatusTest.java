package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class BookingStatusTest {

    @Test
    @DisplayName("예약된 시간과 전체 시간을 비교하여 예약 여부를 구한다.")
    void checkSameReservation_Success() {
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime reservationTime2 = new ReservationTime(2L, LocalTime.of(11, 0));
        List<ReservationTime> reservedTimes = List.of(reservationTime1);
        List<ReservationTime> reservationTimes = List.of(
                reservationTime1,
                reservationTime2
        );
        BookingStatus bookingStatus = BookingStatus.of(reservedTimes, reservationTimes);

        assertAll(
                () -> assertThat(bookingStatus.getReservationStatus().get(reservationTime1)).isTrue(),
                () -> assertThat(bookingStatus.getReservationStatus().get(reservationTime2)).isFalse()
        );
    }
}

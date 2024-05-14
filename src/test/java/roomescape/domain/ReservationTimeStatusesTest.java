package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.reservation.ReservationStatus.AVAILABLE;
import static roomescape.domain.reservation.ReservationStatus.BOOKED;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeStatus;
import roomescape.domain.reservation.ReservationTimeStatuses;

class ReservationTimeStatusesTest {

    private final ReservationTime reservationTime1 = new ReservationTime(1L, "10:30");
    private final ReservationTime reservationTime2 = new ReservationTime(2L, "11:30");
    private final ReservationTime reservationTime3 = new ReservationTime(3L, "12:30");

    @DisplayName("모든 예약 시간과 이미 예약된 시간을 비교하여 가능한 예약 시간 상태를 생성한다")
    @Test
    void calculate_available_reservation_times_status() {
        List<ReservationTime> allTimes = List.of(reservationTime1, reservationTime2, reservationTime3);
        List<ReservationTime> bookedTimes = List.of(reservationTime2);
        ReservationTimeStatuses expectedTimeStatuses = new ReservationTimeStatuses(
                List.of(new ReservationTimeStatus(reservationTime1, AVAILABLE),
                        new ReservationTimeStatus(reservationTime2, BOOKED),
                        new ReservationTimeStatus(reservationTime3, AVAILABLE))
        );

        ReservationTimeStatuses reservationTimeStatuses = new ReservationTimeStatuses(allTimes, bookedTimes);

        assertThat(expectedTimeStatuses)
                .usingRecursiveComparison()
                .isEqualTo(reservationTimeStatuses);
    }
}

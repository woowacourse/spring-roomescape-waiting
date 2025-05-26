package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotTimes;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.waiting.Waiting;

class ReservationSlotTimesTest {

    @Test
    void 예약된_시간은_true_나머지는_false로_설정된다() {
        ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(11, 0));
        ReservationTime time3 = new ReservationTime(3L, LocalTime.of(12, 0));
        ReservationTime time4 = new ReservationTime(4L, LocalTime.of(13, 0));

        List<ReservationTime> allTimes = List.of(time1, time2, time3, time4);

        List<Reservation> alreadyReservedList = List.of(new Reservation(1L, "사용자", LocalDate.now(), time2, null));
        List<Waiting> alreadyWaitingList = List.of(new Waiting(1L, LocalDate.now(), time3, null, null));

        ReservationSlotTimes slotTimes = new ReservationSlotTimes(allTimes, alreadyReservedList, alreadyWaitingList);

        List<ReservationSlot> result = slotTimes.getAvailableBookTimes();

        assertThat(result).extracting(ReservationSlot::isReserved)
                .containsExactly(false, true, true, false);
    }
}

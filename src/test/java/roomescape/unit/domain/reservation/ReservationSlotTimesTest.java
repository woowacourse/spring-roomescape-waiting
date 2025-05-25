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

class ReservationSlotTimesTest {

    @Test
    void 예약된_시간은_true_나머지는_false로_설정된다() {
        ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(2L, LocalTime.of(11, 0));
        ReservationTime time3 = new ReservationTime(3L, LocalTime.of(12, 0));

        List<ReservationTime> allTimes = List.of(time1, time2, time3);

        Reservation alreadyReserved = new Reservation(1L, LocalDate.now(), time2, null);
        List<Reservation> alreadyReservedList = List.of(alreadyReserved);

        ReservationSlotTimes slotTimes = new ReservationSlotTimes(allTimes, alreadyReservedList);

        List<ReservationSlot> result = slotTimes.getAvailableBookTimes();

        assertThat(result).extracting(ReservationSlot::isReserved)
                .containsExactly(false, true, false);
    }
}

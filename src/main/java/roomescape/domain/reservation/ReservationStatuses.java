package roomescape.domain.reservation;

import java.util.ArrayList;
import java.util.List;
import roomescape.domain.ReservationTime;

public class ReservationStatuses {

    private final List<ReservationStatus> reservationStatuses;

    private ReservationStatuses(List<ReservationStatus> reservationStatuses) {
        this.reservationStatuses = reservationStatuses;
    }

    public static ReservationStatuses of(List<ReservationTime> reservedTimes, List<ReservationTime> reservationTimes) {
        List<ReservationStatus> times = reservationTimes.stream()
                .map(time -> new ReservationStatus(time, reservedTimes.contains(time)))
                .toList();
        return new ReservationStatuses(times);
    }

    public List<ReservationStatus> getReservationStatuses() {
        return new ArrayList<>(reservationStatuses);
    }
}

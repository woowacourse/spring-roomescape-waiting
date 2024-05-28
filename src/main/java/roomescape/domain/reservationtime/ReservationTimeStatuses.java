package roomescape.domain.reservationtime;

import java.util.ArrayList;
import java.util.List;

public class ReservationTimeStatuses {

    private final List<ReservationTimeStatus> reservationTimeStatuses;

    private ReservationTimeStatuses(List<ReservationTimeStatus> reservationTimeStatuses) {
        this.reservationTimeStatuses = reservationTimeStatuses;
    }

    public static ReservationTimeStatuses of(List<ReservationTime> reservedTimes, List<ReservationTime> reservationTimes) {
        List<ReservationTimeStatus> times = reservationTimes.stream()
                .map(time -> new ReservationTimeStatus(time, reservedTimes.contains(time)))
                .toList();
        return new ReservationTimeStatuses(times);
    }

    public List<ReservationTimeStatus> getReservationTimeStatuses() {
        return new ArrayList<>(reservationTimeStatuses);
    }
}

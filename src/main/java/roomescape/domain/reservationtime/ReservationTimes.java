package roomescape.domain.reservationtime;

import java.util.List;

public class ReservationTimes {

    private final List<ReservationTime> times;

    private ReservationTimes(List<ReservationTime> times) {
        this.times = List.copyOf(times);
    }

    public static ReservationTimes of(List<ReservationTime> times) {
        return new ReservationTimes(times);
    }

    public List<ReservationTime> availableExcluding(List<Long> bookedTimeIds) {
        return times.stream()
                .filter(time -> !bookedTimeIds.contains(time.getId()))
                .toList();
    }
}

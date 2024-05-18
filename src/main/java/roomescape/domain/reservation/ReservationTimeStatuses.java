package roomescape.domain.reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationTimeStatuses {

    private final List<ReservationTimeStatus> timeStatuses;

    public ReservationTimeStatuses(List<ReservationTimeStatus> timeStatuses) {
        this.timeStatuses = timeStatuses;
    }

    public ReservationTimeStatuses(List<ReservationTime> allTimes, List<ReservationTime> bookedTimes) {
        this(convertToTimeStatuses(allTimes, bookedTimes));
    }

    private static List<ReservationTimeStatus> convertToTimeStatuses(List<ReservationTime> allTimes,
                                                                     List<ReservationTime> bookedTimes) {
        List<ReservationTimeStatus> timeStatuses = new ArrayList<>();
        for (ReservationTime time : allTimes) {
            boolean booked = bookedTimes.contains(time);
            ReservationStatus status = booked ? ReservationStatus.BOOKED : ReservationStatus.AVAILABLE;
            timeStatuses.add(new ReservationTimeStatus(time, status));
        }
        return timeStatuses;
    }

    public List<ReservationTimeStatus> getTimeStatuses() {
        return timeStatuses;
    }
}

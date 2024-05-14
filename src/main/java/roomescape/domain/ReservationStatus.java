package roomescape.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReservationStatus {

    private final Map<ReservationTime, Boolean> reservationStatus;

    private ReservationStatus(Map<ReservationTime, Boolean> reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public static ReservationStatus of(List<ReservationTime> reservedTimes, List<ReservationTime> reservationTimes) {
        Map<ReservationTime, Boolean> reservationStatus = new HashMap<>();
        for (ReservationTime reservationTime : reservationTimes) {
            reservationStatus.put(reservationTime, isReserved(reservedTimes, reservationTime));
        }
        return new ReservationStatus(reservationStatus);
    }

    private static boolean isReserved(List<ReservationTime> reservedTimes, ReservationTime reservationTime) {
        return reservedTimes.stream()
                .anyMatch(reservedTime -> reservedTime.equals(reservationTime));
    }

    public Boolean findReservationStatusBy(ReservationTime reservationTime) {
        return reservationStatus.get(reservationTime);
    }

    public Map<ReservationTime, Boolean> getReservationStatus() {
        return reservationStatus;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ReservationStatus that = (ReservationStatus) object;
        return Objects.equals(reservationStatus, that.reservationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationStatus);
    }
}

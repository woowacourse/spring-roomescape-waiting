package roomescape.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookingStatus {

    private final Map<ReservationTime, Boolean> reservationStatus;

    private BookingStatus(Map<ReservationTime, Boolean> reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public static BookingStatus of(List<ReservationTime> reservedTimes,
                                   List<ReservationTime> reservationTimes) {
        Map<ReservationTime, Boolean> reservationStatus = new HashMap<>();
        for (ReservationTime reservationTime : reservationTimes) {
            reservationStatus.put(reservationTime, isReserved(reservedTimes, reservationTime));
        }
        return new BookingStatus(reservationStatus);
    }

    private static boolean isReserved(List<ReservationTime> reservedTimes,
                                      ReservationTime reservationTime) {
        return reservedTimes.contains(reservationTime);
    }

    public Map<ReservationTime, Boolean> getReservationStatus() {
        return reservationStatus;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        BookingStatus that = (BookingStatus) object;
        return Objects.equals(reservationStatus, that.reservationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationStatus);
    }
}

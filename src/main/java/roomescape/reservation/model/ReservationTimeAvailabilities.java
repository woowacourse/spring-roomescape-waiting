package roomescape.reservation.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ReservationTimeAvailabilities(Map<ReservationTime, Boolean> values) {

    public static ReservationTimeAvailabilities of(final List<ReservationTime> reservationTimes, final List<Reservation> reservations) {
        final Map<ReservationTime, Boolean> reservationTimeAvailabilities = new HashMap<>();
        reservationTimes.forEach(reservationTime -> reservationTimeAvailabilities.put(
                reservationTime, isTimeAvailable(reservations, reservationTime)));

        return new ReservationTimeAvailabilities(reservationTimeAvailabilities);
    }

    private static boolean isTimeAvailable(final List<Reservation> reservations, final ReservationTime reservationTime) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTime().equals(reservationTime));
    }
}

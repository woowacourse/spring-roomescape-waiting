package roomescape.dto.reservation;

import java.util.ArrayList;
import java.util.List;

public record MyReservationsResponse(
) {
    public static List<MyReservationResponse> combine(List<MyReservationResponse> reservations,
                                                      List<MyReservationResponse> waitings) {
        final ArrayList<MyReservationResponse> myReservations = new ArrayList<>(reservations);
        myReservations.addAll(waitings);
        return myReservations;
    }
}

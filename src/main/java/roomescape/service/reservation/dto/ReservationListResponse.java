package roomescape.service.reservation.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class ReservationListResponse {
    private final List<ReservationResponse> reservations;

    @JsonCreator
    public ReservationListResponse(List<ReservationResponse> reservations) {
        this.reservations = reservations;
    }

    public List<ReservationResponse> getReservations() {
        return reservations;
    }
}

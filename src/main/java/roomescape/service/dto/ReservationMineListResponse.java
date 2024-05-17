package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class ReservationMineListResponse {
    private final List<ReservationMineResponse> reservations;

    @JsonCreator
    public ReservationMineListResponse(List<ReservationMineResponse> reservations) {
        this.reservations = reservations;
    }

    public List<ReservationMineResponse> getReservations() {
        return reservations;
    }
}

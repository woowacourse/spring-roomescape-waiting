package roomescape.service.dto.time;

import roomescape.service.dto.reservation.ReservationTimeResponse;

public class AvailableTimeResponse {

    private final ReservationTimeResponse timeResponseDto;
    private final boolean booked;

    public AvailableTimeResponse(ReservationTimeResponse time, boolean booked) {
        this.timeResponseDto = time;
        this.booked = booked;
    }

    public ReservationTimeResponse getTimeResponseDto() {
        return timeResponseDto;
    }

    public boolean isBooked() {
        return booked;
    }
}

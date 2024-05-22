package roomescape.service.dto.time;

public class AvailableTimeResponse {

    private final ReservationTimeResponse timeResponseDto;
    private final boolean booked;

    public AvailableTimeResponse(ReservationTimeResponse timeResponseDto, boolean booked) {
        this.timeResponseDto = timeResponseDto;
        this.booked = booked;
    }

    public ReservationTimeResponse getTimeResponseDto() {
        return timeResponseDto;
    }

    public boolean isBooked() {
        return booked;
    }
}

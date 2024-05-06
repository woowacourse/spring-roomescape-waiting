package roomescape.application.reservation.dto.response;

public record AvailableTimeResponse(ReservationTimeResponse time, boolean isBooked) {
}

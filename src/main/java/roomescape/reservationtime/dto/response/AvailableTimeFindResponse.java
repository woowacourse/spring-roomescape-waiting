package roomescape.reservationtime.dto.response;

public record AvailableTimeFindResponse(
        TimeInformation timeInformation,
        boolean isAvailable
) {
}

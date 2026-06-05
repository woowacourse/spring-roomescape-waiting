package roomescape.reservationtime.application.dto.response;

public record AvailableTimeFindResponse(
        TimeInformation timeInformation,
        TimeSlotStatus status
) {
}

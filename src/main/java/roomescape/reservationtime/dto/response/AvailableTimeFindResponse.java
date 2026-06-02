package roomescape.reservationtime.dto.response;

public record AvailableTimeFindResponse(
        TimeInformation timeInformation,
        TimeSlotStatus status
) {
}

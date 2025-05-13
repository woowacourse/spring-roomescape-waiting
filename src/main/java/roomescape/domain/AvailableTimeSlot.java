package roomescape.domain;

public record AvailableTimeSlot(
    TimeSlot timeSlot,
    boolean alreadyBooked
) {

}

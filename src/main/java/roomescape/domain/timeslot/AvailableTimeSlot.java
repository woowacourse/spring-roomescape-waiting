package roomescape.domain.timeslot;

public record AvailableTimeSlot(
        TimeSlot timeSlot,
        boolean alreadyBooked
) {

}

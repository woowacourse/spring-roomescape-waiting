package roomescape.domain.timeslot;

public record TimeSlotBookStatus(
        TimeSlot timeSlot,
        boolean alreadyBooked
) {

}

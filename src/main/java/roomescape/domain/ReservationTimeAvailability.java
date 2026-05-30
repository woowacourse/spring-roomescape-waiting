package roomescape.domain;

public record ReservationTimeAvailability(
        ReservationTime time,
        boolean available
) {
}

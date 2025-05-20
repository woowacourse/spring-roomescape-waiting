package roomescape.reservation.domain;

public record ReservationWithRank(
        Reservation reservation,
        Long rank
) {
}

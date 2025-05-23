package roomescape.reservation.domain;

public record ReservationWithRank(
        Reservation reservation,
        Long rank
) {

    public boolean isReserved() {
        return reservation.isReserved();
    }

    public String getReservationStatus() {
        return reservation.getReservationStatus();
    }
}

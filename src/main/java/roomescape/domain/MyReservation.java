package roomescape.domain;

public record MyReservation(Reservation reservation, Long waitingNumber) {
    public boolean isWaiting() {
        return waitingNumber != null;
    }
}

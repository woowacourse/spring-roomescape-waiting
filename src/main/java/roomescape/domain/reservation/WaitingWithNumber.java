package roomescape.domain.reservation;

public record WaitingWithNumber(
        Reservation waiting,
        int waitingIndex
) {
}

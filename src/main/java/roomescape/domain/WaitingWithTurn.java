package roomescape.domain;

public record WaitingWithTurn(
        ReservationWaiting waiting,
        Long turn
) {
}

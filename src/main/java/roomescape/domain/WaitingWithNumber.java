package roomescape.domain;

public record WaitingWithNumber(
        Reservation waiting,
        int number
) {
}

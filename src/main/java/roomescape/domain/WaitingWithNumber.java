package roomescape.domain;

public record WaitingWithNumber(
        Reservation waiting,
        WaitingNumber number
) {
}

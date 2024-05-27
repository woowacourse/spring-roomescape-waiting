package roomescape.exception.reservation;

public class WaitingListExceededException extends ReservationException {
    private static final String LOG_MESSAGE_FORMAT =
            "Waiting list exceeded for Reservation #%d";

    public WaitingListExceededException(long reservationId) {
        super(
                "대기 인원이 초과되었습니다.",
                LOG_MESSAGE_FORMAT.formatted(reservationId)
        );
    }
}

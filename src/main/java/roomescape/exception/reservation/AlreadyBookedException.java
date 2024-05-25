package roomescape.exception.reservation;

import java.time.LocalDate;

public class AlreadyBookedException extends ReservationException {
    private static final String LOG_MESSAGE_FORMAT =
            "Failed to book Reservation on %s at Time #%d with Theme #%d: Already booked";

    public AlreadyBookedException(LocalDate date, long timeId, long themeId) {
        super(
                "이미 존재하는 예약입니다.",
                LOG_MESSAGE_FORMAT.formatted(date, timeId, themeId)
        );
    }
}

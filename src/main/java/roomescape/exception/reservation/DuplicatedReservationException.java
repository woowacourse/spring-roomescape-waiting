package roomescape.exception.reservation;

import java.time.LocalDate;

public class DuplicatedReservationException extends ReservationException {
    private static final String LOG_MESSAGE_FORMAT =
            "Failed to book Reservation (theme #%d, date %s, time #%d: Duplicated booking or waiting)";

    public DuplicatedReservationException(long themeId, LocalDate date, long timeId) {
        super(
                "이미 예약했거나 대기한 항목입니다.",
                LOG_MESSAGE_FORMAT.formatted(themeId, date, timeId)
        );
    }
}

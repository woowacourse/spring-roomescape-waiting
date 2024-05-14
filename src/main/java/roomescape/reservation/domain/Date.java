package roomescape.reservation.domain;

import java.time.LocalDate;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.exception.ReservationExceptionCode;

public class Date {

    private final LocalDate date;

    private Date(LocalDate date) {
        this.date = date;
    }

    public static Date saveDateFrom(LocalDate date) {
        validateAtSave(date);
        return new Date(date);
    }

    public static Date dateFrom(LocalDate date) {
        return new Date(date);
    }

    public LocalDate getDate() {
        return date;
    }

    private static void validateAtSave(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new RoomEscapeException(ReservationExceptionCode.RESERVATION_DATE_IS_PAST_EXCEPTION);
        }
    }

    @Override
    public String toString() {
        return "Date{" +
                "date=" + date +
                '}';
    }
}

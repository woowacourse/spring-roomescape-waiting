package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.exception.ReservationExceptionCode;

@Embeddable
public class Date {

    @Column(nullable = false)
    private LocalDate date;

    protected Date() {
    }

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

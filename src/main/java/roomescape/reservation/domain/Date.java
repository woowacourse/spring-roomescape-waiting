package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.reservation.exception.ReservationExceptionCode;

@Embeddable
public class Date {

    private static final int CANCEL_MIN_DATE = 2;
    private static final int RESERVATION_POSSIBLE_MAX_DATE = 7;

    @Column(nullable = false)
    private LocalDate date;

    protected Date() {
    }

    private Date(LocalDate date) {
        this.date = date;
    }

    public static Date saveFrom(LocalDate date) {
        validateAtSave(date);
        return new Date(date);
    }

    public static Date dateFrom(LocalDate date) {
        return new Date(date);
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isAfterCancelDate(LocalDate now) {
        return now.isAfter(date.minusDays(CANCEL_MIN_DATE));
    }

    private static void validateAtSave(LocalDate date) {
        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            throw new RoomEscapeException(ReservationExceptionCode.RESERVATION_DATE_IS_PAST_EXCEPTION);
        }
        if (date.isAfter(today.plusDays(RESERVATION_POSSIBLE_MAX_DATE))) {
            throw new RoomEscapeException(ReservationExceptionCode.RESERVATION_DATE_IS_OVER_RANGE_EXCEPTION);
        }
    }

    @Override
    public String toString() {
        return "Date{" +
                "date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Date date1 = (Date) o;
        return Objects.equals(date, date1.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}

package roomescape.domain.reservationdate;

import java.time.LocalDate;
import lombok.Getter;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.RoomescapeException;

@Getter
public class ReservationDate {

    private final Long id;
    private final LocalDate playDay;

    private ReservationDate(Long id, LocalDate playDay) {
        validate(playDay);
        this.id = id;
        this.playDay = playDay;
    }

    private ReservationDate(LocalDate playDay) {
        this(null, playDay);
    }

    public static ReservationDate of(Long dateId, LocalDate playDay) {
        return new ReservationDate(dateId, playDay);
    }

    public static ReservationDate createWithoutId(LocalDate playDay) {
        return new ReservationDate(playDay);
    }

    private static void validate(LocalDate playDay) {
        if (playDay == null) {
            throw new RoomescapeException(ReservationDateErrorCode.INVALID_RESERVATION_DATE);
        }
    }

    public boolean isAvailable(LocalDate targetDate) {
        return !playDay.isBefore(targetDate);
    }
}

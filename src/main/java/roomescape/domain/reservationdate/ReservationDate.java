package roomescape.domain.reservationdate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.support.exception.ReservationDateErrorCode;
import roomescape.support.exception.RoomescapeException;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate playDay;

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

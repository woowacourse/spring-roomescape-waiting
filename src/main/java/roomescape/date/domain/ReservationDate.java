package roomescape.date.domain;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_IS_NULL;
import static roomescape.date.exception.ReservationDateErrorInformation.ID_IS_NULL;
import static roomescape.date.exception.ReservationDateErrorInformation.INACTIVE_DATE_NOT_ALLOWED;
import static roomescape.date.exception.ReservationDateErrorInformation.PAST_DATE_NOT_ALLOWED;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.date.exception.ReservationDateException;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationDate {

    private Long id;
    private LocalDate date;
    private boolean isActive;

    public static ReservationDate create(LocalDate date) {
        validateDate(date);
        return new ReservationDate(null, date, true);
    }

    public static ReservationDate load(Long id, LocalDate date, boolean isActive) {
        validateId(id);
        return new ReservationDate(id, date, isActive);
    }

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new ReservationDateException(ID_IS_NULL);
        }
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new ReservationDateException(DATE_IS_NULL);
        }

        if (date.isBefore(LocalDate.now())) {
            throw new ReservationDateException(PAST_DATE_NOT_ALLOWED);
        }
    }

    public void validateIsInactive() {
        if (!isActive) {
            throw new ReservationDateException(INACTIVE_DATE_NOT_ALLOWED);
        }
    }

}

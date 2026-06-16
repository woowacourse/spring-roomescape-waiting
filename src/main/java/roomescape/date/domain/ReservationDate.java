package roomescape.date.domain;

import static roomescape.date.exception.ReservationDateErrorInformation.DATE_IS_NULL;
import static roomescape.date.exception.ReservationDateErrorInformation.ID_IS_NULL;
import static roomescape.date.exception.ReservationDateErrorInformation.INACTIVE_DATE_NOT_ALLOWED;
import static roomescape.date.exception.ReservationDateErrorInformation.PAST_DATE_NOT_ALLOWED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.date.exception.ReservationDateException;

@Entity(name = "reservation_date")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ReservationDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "is_active", nullable = false)
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

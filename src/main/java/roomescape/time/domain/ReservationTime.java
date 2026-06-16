package roomescape.time.domain;

import static roomescape.time.exception.ReservationTimeErrorInformation.ID_IS_NULL;
import static roomescape.time.exception.ReservationTimeErrorInformation.INACTIVE_TIME_NOT_ALLOWED;
import static roomescape.time.exception.ReservationTimeErrorInformation.START_AT_IS_NULL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.time.exception.ReservationTimeException;

@Entity(name = "reservation_time")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalTime startAt;

    @Column
    private boolean isActive;

    public static ReservationTime create(LocalTime startAt) {
        validateStartAt(startAt);
        return new ReservationTime(null, startAt, true);
    }

    public static ReservationTime load(Long timeId, LocalTime startAt, boolean isActive) {
        validateStartAt(startAt);
        validateId(timeId);
        return new ReservationTime(timeId, startAt, isActive);
    }

    private static void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new ReservationTimeException(START_AT_IS_NULL);
        }
    }

    private static void validateId(Long timeId) {
        if (timeId == null) {
            throw new ReservationTimeException(ID_IS_NULL);
        }
    }

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

    public void validateIsInactive() {
        if (!isActive) {
            throw new ReservationTimeException(INACTIVE_TIME_NOT_ALLOWED);
        }
    }

}

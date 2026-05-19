package roomescape.time.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.time.exception.ReservationTimeException;

import java.time.LocalTime;

import static roomescape.time.exception.ReservationTimeErrorInformation.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationTime {

    private Long id;
    private LocalTime startAt;
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

package roomescape.domain.reservationtime;

import java.time.LocalTime;
import lombok.Getter;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationTimeErrors;

@Getter
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private ReservationTime(LocalTime startAt) {
        validate(startAt);
        this.id = null;
        this.startAt = startAt;
    }

    public static ReservationTime createWithoutId(LocalTime startAt) {
        return new ReservationTime(startAt);
    }

    public static ReservationTime of(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    private static void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new BadRequestException(ReservationTimeErrors.INVALID_RESERVATION_TIME);
        }
    }

    public boolean isBefore(LocalTime compareTime) {
        return startAt.isBefore(compareTime);
    }
}

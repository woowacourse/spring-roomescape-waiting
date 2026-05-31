package roomescape.domain.reservationtime;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;

@Getter
public class ReservationTime {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public static ReservationTime createWithoutId(LocalTime startAt) {
        return new ReservationTime(startAt);
    }

    public static ReservationTime of(Long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    private static void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new RoomescapeException(ReservationTimeErrorCode.INVALID_RESERVATION_TIME);
        }
    }

    public String getFormattedStartAt() {
        return startAt.format(TIME_FORMATTER);
    }

}

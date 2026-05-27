package roomescape.domain.reservationtime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final LocalTime finishAt;

    private ReservationTime(Long id, LocalTime startAt, LocalTime finishAt) {
        this.id = id;
        this.startAt = startAt;
        this.finishAt = finishAt;
    }

    public static ReservationTime of(Long id, LocalTime startAt, LocalTime finishAt) {
        return new ReservationTime(id, startAt, finishAt);
    }

    public static ReservationTime of(LocalTime startAt, LocalTime finishAt) {
        return new ReservationTime(null, startAt, finishAt);
    }

    public void validateIfTimePast(LocalDate reservationDate) {
        LocalDateTime now = LocalDateTime.now();
        if (reservationDate.isBefore(now.toLocalDate())
                || (reservationDate.isEqual(now.toLocalDate()) && startAt.isBefore(now.toLocalTime()))) {
            throw new RoomescapeException(ErrorCode.RESERVATION_TIME_PASSED);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public LocalTime getFinishAt() {
        return finishAt;
    }

}

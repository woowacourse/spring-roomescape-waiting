package roomescape.reservation;

import java.time.LocalDate;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.time.ReservationTime;
import roomescape.waiting.ReservationWaiting;

public class Reservation {
    private Long id;
    private final String name;
    private final Long themeId;
    private final LocalDate date;
    private final ReservationTime time;
    private final ReservationStatus status;

    public Reservation(String name, Long themeId, LocalDate date, ReservationTime time) {
        this(null, name, themeId, date, time, ReservationStatus.CONFIRMED);
    }

    public Reservation(Long id, String name, Long themeId, LocalDate date, ReservationTime time) {
        this(id, name, themeId, date, time, ReservationStatus.CONFIRMED);
    }

    public Reservation(Long id, String name, Long themeId, LocalDate date, ReservationTime time,
                       ReservationStatus status) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.themeId = themeId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Long getThemeId() {
        return themeId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void validateSameName(String name, ErrorCode errorCode) {
        if (!this.name.equals(name)) {
            throw new RoomescapeException(errorCode);
        }
    }

    public void validateDateTime(LocalDate date, ReservationTime time, ErrorCode errorCode) {
        if (time.isBeforeDateTime(date, time)) {
            throw new RoomescapeException(errorCode);
        }
    }

    public Reservation approve(ReservationWaiting waiting) {
        validateSameSchedule(waiting);
        return new Reservation(id, waiting.getName(), themeId, date, time, ReservationStatus.PENDING_PAYMENT);
    }

    private void validateSameSchedule(ReservationWaiting waiting) {
        if (!themeId.equals(waiting.getThemeId())
                || !date.equals(waiting.getDate())
                || !time.getId().equals(waiting.getTime().getId())) {
            throw new RoomescapeException(ErrorCode.INVALID_RESERVATION_WAITING);
        }
    }
}

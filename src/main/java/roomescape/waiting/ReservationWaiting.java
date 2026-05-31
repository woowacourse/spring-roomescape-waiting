package roomescape.waiting;

import roomescape.exception.RoomescapeException;
import roomescape.time.ReservationTime;

import java.time.LocalDate;

import static roomescape.exception.ErrorCode.FORBIDDEN_RESERVATION_WAITING_ACCESS;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final Long themeId;
    private final LocalDate date;
    private final ReservationTime time;
    private final Long waitingNumber;

    public ReservationWaiting(Long id, String name, Long themeId, LocalDate date, ReservationTime reservationTime, Long waitingNumber) {
        this.id = id;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.time = reservationTime;
        this.waitingNumber = waitingNumber;
    }

    public ReservationWaiting(String name, Long themeId, LocalDate date, ReservationTime reservationTime, Long waitingNumber) {
        this.id = null;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.time = reservationTime;
        this.waitingNumber = waitingNumber;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getThemeId() {
        return themeId;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }

    public void validateSameName(String name){
        if (!this.name.equals(name)) {
            throw new RoomescapeException(FORBIDDEN_RESERVATION_WAITING_ACCESS);
        }
    }
}

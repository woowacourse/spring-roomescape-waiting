package roomescape.waiting;

import roomescape.global.exception.RoomescapeException;
import roomescape.time.ReservationTime;

import java.time.LocalDate;

import static roomescape.global.exception.ErrorCode.UNAUTHORIZED_RESERVATION_WAITING_ACCESS;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final Long themeId;
    private final LocalDate date;
    private final ReservationTime reservationTime;
    private final Long waitingNumber;

    public ReservationWaiting(Long id, String name, Long themeId, LocalDate date, ReservationTime reservationTime, Long waitingNumber) {
        this.id = id;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.reservationTime = reservationTime;
        this.waitingNumber = waitingNumber;
    }

    public ReservationWaiting(String name, Long themeId, LocalDate date, ReservationTime reservationTime, Long waitingNumber) {
        this.id = null;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.reservationTime = reservationTime;
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

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }

    public void validateSameName(String name){
        if (!this.name.equals(name)) {
            throw new RoomescapeException(UNAUTHORIZED_RESERVATION_WAITING_ACCESS);
        }
    }
}

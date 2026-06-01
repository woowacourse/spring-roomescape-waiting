package roomescape.waiting;

import static roomescape.exception.ErrorCode.FORBIDDEN_RESERVATION_WAITING_ACCESS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.RoomescapeException;
import roomescape.time.ReservationTime;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final Long themeId;
    private final LocalDate date;
    private final ReservationTime time;
    private final LocalDateTime createdAt;
    private final Long waitingNumber;

    public ReservationWaiting(String name, Long themeId, LocalDate date, ReservationTime reservationTime,
                              LocalDateTime createdAt) {
        this.id = null;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.time = reservationTime;
        this.createdAt = createdAt;
        this.waitingNumber = null;
    }

    public ReservationWaiting(Long id, String name, Long themeId, LocalDate date, ReservationTime reservationTime,
                              LocalDateTime createdAt, Long waitingNumber) {
        this.id = id;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.time = reservationTime;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }

    public void validateSameName(String name) {
        if (!this.name.equals(name)) {
            throw new RoomescapeException(FORBIDDEN_RESERVATION_WAITING_ACCESS);
        }
    }
}

package roomescape.waiting;

import java.time.LocalDate;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final Long themeId;
    private final LocalDate date;
    private final Long timeId;
    private final Long waitingNumber;

    public ReservationWaiting(Long id, String name, Long themeId, LocalDate date, Long timeId, Long waitingNumber) {
        this.id = id;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.timeId = timeId;
        this.waitingNumber = waitingNumber;
    }

    public ReservationWaiting(String name, Long themeId, LocalDate date, Long timeId, Long waitingNumber) {
        this.id = null;
        this.name = name;
        this.themeId = themeId;
        this.date = date;
        this.timeId = timeId;
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

    public Long getTimeId() {
        return timeId;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }
}

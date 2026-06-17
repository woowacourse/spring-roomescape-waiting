package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.theme.Theme;
import roomescape.waiting.ReservationWaiting;

public class MyReservation {
    private static final String RESERVED_RESOURCE = "reservation";
    private static final String WAITING_RESOURCE = "waiting";
    private static final String WAITING_STATUS = "대기중";

    private final Long id;
    private final String name;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime startAt;
    private final String resourceType;
    private final String status;
    private final Long waitingNumber;

    public MyReservation(Reservation reservation, Theme theme) {
        this.id = reservation.getId();
        this.name = reservation.getName();
        this.themeName = theme.getName();
        this.date = reservation.getDate();
        this.startAt = reservation.getTime().getStartAt();
        this.resourceType = RESERVED_RESOURCE;
        this.status = reservation.getStatus().getDescription();
        this.waitingNumber = null;
    }

    public MyReservation(ReservationWaiting waiting, Theme theme) {
        this.id = waiting.getId();
        this.name = waiting.getName();
        this.themeName = theme.getName();
        this.date = waiting.getDate();
        this.startAt = waiting.getTime().getStartAt();
        this.resourceType = WAITING_RESOURCE;
        this.status = WAITING_STATUS;
        this.waitingNumber = waiting.getWaitingNumber();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThemeName() {
        return themeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getStatus() {
        return status;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }
}

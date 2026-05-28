package roomescape.reservation.domain;

import roomescape.reservation.exception.ForbiddenRequestException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;

public class Reservation {
    private final Long id;
    private final String name;
    private final ReservationTime time;
    private final Theme theme;
    private final Status status;
    private final LocalDateTime createdAt;

    public Reservation(String name, ReservationTime time, Theme theme, Status status, LocalDateTime createdAt) {
        this(null, name, time, theme, status, createdAt);
    }

    private Reservation(Long id, String name, ReservationTime time, Theme theme, Status status,
                        LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.name, this.time, this.theme, this.status, this.createdAt);
    }

    public Reservation withTime(ReservationTime time) {
        return new Reservation(this.id, this.name, time, this.theme, this.status, this.createdAt);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isReserved() {
        return this.status.equals(Status.RESERVED);
    }

    public void validateOwnedBy(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenRequestException();
        }
    }
}

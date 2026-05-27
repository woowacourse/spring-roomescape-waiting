package roomescape.domain.reservation;

import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.ExpiredDateTimeException;

public class Reservation {

    private Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LocalDateTime createdAt;

    public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = LocalDateTime.now();
    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public Reservation withReservationId(Long id) {
        return new Reservation(id, this.name, this.date, this.time, this.theme, this.createdAt);
    }

    public Reservation withUpdatedDateAndTime(LocalDate date, ReservationTime time) {
        return new Reservation(id, this.name, date, time, this.theme, this.createdAt);
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

    public Theme getTheme() {
        return theme;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isReserved(String name) {
        return this.name.equals(name);
    }

    public void validatePastDateTime() {
        if(LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new ExpiredDateTimeException();
        }
    }
}

package roomescape.domain.reservation;

import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.ExpiredDateTimeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final LocalDateTime createdAt;

    private Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public static Reservation create(String name, LocalDate date, ReservationTime time, Theme theme) {
        validatePastDateTime(date, time.getStartAt());
        return new Reservation(null, name, date, time, theme, LocalDateTime.now());
    }

    public static Reservation restore(Long id, String name, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        return new Reservation(id, name, date, time, theme, createdAt);
    }

    public Reservation update(String name, LocalDate date, ReservationTime time, Theme theme) {
        validateModifiable();
        validatePastDateTime(date, time.getStartAt());
        return Reservation.create(name, date, time, theme);
    }

    private void validateModifiable() {
        validatePastDateTime(date, time.getStartAt());
    }

    public Reservation withReservationId(Long id) {
        return new Reservation(id, this.name, this.date, this.time, this.theme, this.createdAt);
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

    public boolean isReservedBy(String name) {
        return this.name.equals(name);
    }

    public boolean isExpired() {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now());
    }

    private static void validatePastDateTime(LocalDate date, LocalTime time) {
        if(LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new ExpiredDateTimeException();
        }
    }
}

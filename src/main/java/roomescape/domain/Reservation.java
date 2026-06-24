package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final ReservationStatus reservationStatus;

    public Reservation(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            ReservationStatus reservationStatus
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = reservationStatus;
    }

    public Reservation(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(null, name, date, time, theme, ReservationStatus.PENDING);
    }

    public boolean isOwnedBy(String name) {
        return name.equals(this.name);
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public boolean isSameTime(ReservationTime time) {
        return this.time.equals(time);
    }

    public boolean isPending() {
        return reservationStatus == ReservationStatus.PENDING;
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

    public Long getThemeId() {
        return theme.getId();
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

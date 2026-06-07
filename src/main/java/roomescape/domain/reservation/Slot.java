package roomescape.domain.reservation;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.theme.Theme;

public class Slot {
    private final long id;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Slot(long id, ReservationDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
    }

    public static Slot load(long id, ReservationDate date, ReservationTime time, Theme theme) {
        return new Slot(id, date, time, theme);
    }

    public static Slot create(ReservationDate date, ReservationTime time, Theme theme) {
        return new Slot(0, date, time, theme);
    }

    public Slot withId(long id) {
        return new Slot(id, date, time, theme);
    }

    public boolean isSame(Slot target) {
        return id == target.id;
    }

    public boolean isBefore(LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date.getValue(), time.getStartAt());

        return reservationDateTime.isBefore(now);
    }

    public long getId() {
        return id;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Slot slot = (Slot) o;
        return id == slot.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

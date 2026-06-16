package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ThemeSlot {

    private final Long id;
    private final Theme theme;
    private final LocalDate date;
    private final Time time;
    private boolean isReserved;
    private final Reservations reservations;

    public ThemeSlot(Theme theme, LocalDate date, Time time, boolean isReserved) {
        this.id = null;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.isReserved = isReserved;
        this.reservations = new Reservations(List.of());
    }

    public ThemeSlot(Long id, Theme theme, LocalDate date, Time time, boolean isReserved) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.isReserved = isReserved;
        this.reservations = new Reservations(List.of());
    }

    public ThemeSlot(Long id, Theme theme, LocalDate date, Time time, boolean isReserved, List<Reservation> reservations) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.isReserved = isReserved;
        this.reservations = new Reservations(reservations);
    }

    public static ThemeSlot of(Long id, ThemeSlot themeSlot) {
        return new ThemeSlot(id, themeSlot.getTheme(), themeSlot.getDate(), themeSlot.getTime(), themeSlot.isReserved());
    }

    public Reservation addReservation(String name) {
        reservations.validateDuplicate(name);
        Reservation reservation = new Reservation(name, this.id, this.date, this.time, this.theme);
        reservations.add(reservation);
        return reservation;
    }

    public Optional<Reservation> cancelReservation(Long reservationId) {
        Reservation target = reservations.findById(reservationId);
        boolean wasConfirmed = target.isConfirmed();
        target.cancel();
        if (wasConfirmed) {
            Optional<Reservation> promoted = reservations.findFirstPending();
            promoted.ifPresentOrElse(Reservation::confirm, () -> this.isReserved = false);
            return promoted;
        }
        return Optional.empty();
    }

    public Reservation findReservationById(Long id) {
        return reservations.findById(id);
    }

    public Long getId() {
        return id;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public Reservations getReservations() {
        return reservations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeSlot that = (ThemeSlot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

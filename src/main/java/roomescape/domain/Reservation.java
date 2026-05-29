package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

    private final Long id;
    private final String username;
    private final Slot slot;

    private Reservation(Long id, String username, Slot slot) {
        this.id = id;
        this.username = username;
        this.slot = slot;
    }

    public static Reservation from(long id, String username, Slot slot) {
        return new Reservation(id, username, slot);
    }

    public static Reservation create(String username, Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new Reservation (null, username, slot);
    }

    public boolean isOwnedBy(String name) {
        return this.username.equals(name);
    }

    public Reservation withSlot(Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new Reservation(this.id, this.username, slot);
    }

    public void validateCancelable(LocalDateTime now) {
        slot.validateAvailableTime(now);
    }

    public String username() {
        return username;
    }

    public LocalDate reservationDate() {
        return slot.date();
    }

    public ReservationTime reservationTime() {
        return slot.time();
    }

    public Theme reservationTheme() {
        return slot.theme();
    }

    public long id() {
        return id;
    }
}

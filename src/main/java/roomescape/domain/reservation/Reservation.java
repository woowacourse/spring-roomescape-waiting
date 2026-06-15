package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final Slot slot;

    private final LocalDateTime createdAt;
    private final boolean paid;

    private Reservation(Long id, String name, Slot slot, LocalDateTime createdAt, boolean paid) {
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.createdAt = createdAt;
        this.paid = paid;
    }

    public static Reservation create(String name, Slot slot) {
        validateNotExpired(slot);
        return new Reservation(null, name, slot, LocalDateTime.now(), false);
    }

    public static Reservation restore(Long id, Slot slot, String name, LocalDateTime createdAt, boolean paid) {
        return new Reservation(id, name, slot, createdAt, paid);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.name, this.slot, this.createdAt, this.paid);
    }

    public Reservation update(String name) {
        validateNotExpired(this.slot);
        return new Reservation(this.id, name, this.slot, this.createdAt, this.paid);
    }

    public Reservation update(String name, Slot slot) {
        validateNotExpired(this.slot);
        validateNotExpired(slot);
        return new Reservation(this.id, name, slot, LocalDateTime.now(), this.paid);
    }

    public Reservation updatePaid(boolean paid) {
        return new Reservation(this.id, this.name, this.slot, this.createdAt, paid);
    }

    public boolean isReservedBy(String name) {
        return this.name.equals(name);
    }

    public boolean isExpired() {
        return slot.isExpired();
    }

    public boolean isSameSlot(LocalDate date, Long timeId, Long themeId) {
        return slot.isEqualSlot(date, timeId, themeId);
    }

    private static void validateNotExpired(Slot slot) {
        if (slot.isExpired()) {
            throw new ExpiredDateTimeException();
        }
    }

    public Long getId() {
        return id;
    }

    public Slot getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isPaid() {
        return paid;
    }

    public long getSlotId() {
        return slot.getId();
    }
}

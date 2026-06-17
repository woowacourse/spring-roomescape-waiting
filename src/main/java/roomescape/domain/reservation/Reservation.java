package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class Reservation {

    private final Long id;
    private final Slot slot;
    private final String name;
    private final LocalDateTime createdAt;
    private final boolean paid;

    private Reservation(Long id, Slot slot, String name, LocalDateTime createdAt, boolean paid) {
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.createdAt = createdAt;
        this.paid = paid;
    }

    public static Reservation create(String name, Slot slot) {
        validateNotExpired(slot);
        return new Reservation(null, slot, name, LocalDateTime.now(), false);
    }

    public static Reservation restore(Long id, Slot slot, String name, LocalDateTime createdAt) {
        return new Reservation(id, slot, name, createdAt, false);
    }

    public static Reservation restore(Long id, Slot slot, String name, LocalDateTime createdAt, boolean paid) {
        return new Reservation(id, slot, name, createdAt, paid);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, this.slot, this.name, this.createdAt, this.paid);
    }

    public Reservation update(String name) {
        validateNotExpired(this.slot);
        return new Reservation(this.id, this.slot, name, this.createdAt, this.paid);
    }

    public Reservation update(String name, Slot slot) {
        validateNotExpired(slot);
        return new Reservation(this.id, slot, name, LocalDateTime.now(), this.paid);
    }

    public Reservation updatePaid(boolean paid) {
        return new Reservation(this.id, this.slot, this.name, this.createdAt, paid);
    }

    public Reservation confirmPayment() {
        return new Reservation(this.id, this.slot, this.name, this.createdAt, true);
    }

    public boolean isPaid() {
        return paid;
    }

    public Long getSlotId() {
        return slot.getId();
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
}

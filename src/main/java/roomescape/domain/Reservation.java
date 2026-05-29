package roomescape.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import roomescape.exception.ForbiddenException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Reservation {
    private final Long id;
    private final String username;
    private final Slot slot;

    public static Reservation from(long id, String username, Slot slot) {
        return new Reservation(id, username, slot);
    }

    public static Reservation create(String username, Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new Reservation(null, username, slot);
    }

    public boolean isOwnedBy(String name) {
        return this.username.equals(name);
    }

    public void validateOwnedBy(String name) {
        if (!this.isOwnedBy(name)) {
            throw new ForbiddenException("타인의 예약에 접근할 수 없습니다.");
        }
    }

    public Reservation withSlot(Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new Reservation(this.id, this.username, slot);
    }

    public void validateCancelable(LocalDateTime now) {
        slot.validateAvailableTime(now);
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getReservationDate() {
        return slot.getDate();
    }

    public ReservationTime getReservationTime() {
        return slot.getTime();
    }

    public Theme getReservationTheme() {
        return slot.getTheme();
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

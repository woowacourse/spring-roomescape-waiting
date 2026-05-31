package roomescape.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Reservation {

    private final Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime createdAt;
    private ReservationStatus status;

    public Reservation(Long id, String name, ReservationSlot slot, ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Reservation reserve(String name, ReservationSlot slot) {
        return new Reservation(null, name, slot, ReservationStatus.RESERVED, LocalDateTime.now());
    }

    public static Reservation waiting(String name, ReservationSlot slot) {
        return new Reservation(null, name, slot, ReservationStatus.WAITING, LocalDateTime.now());
    }

    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }

    public boolean isSameId(long id) {
        return this.id != null && this.id.equals(id);
    }

    public void cancel() {
        this.status = ReservationStatus.DELETED;
    }

    public boolean isWaiting() {
        return this.status == ReservationStatus.WAITING;
    }

    public boolean isDeleted() {
        return this.status == ReservationStatus.DELETED;
    }

    public void promote() {
        this.status = ReservationStatus.RESERVED;
    }

    public boolean hasSameName(String name) {
        return this.name.equals(name);
    }

    public boolean matches(String name, ReservationStatus status) {
        return this.name.equals(name) && this.status == status;
    }
}

package roomescape.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private ReservationStatus status;

    public Reservation(Long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Reservation reserve(String name) {
        return new Reservation(null, name, ReservationStatus.RESERVED, LocalDateTime.now());
    }

    public static Reservation waiting(String name) {
        return new Reservation(null, name, ReservationStatus.WAITING, LocalDateTime.now());
    }

    public void cancel() {
        this.status = ReservationStatus.DELETED;
    }

    public void promote() {
        this.status = ReservationStatus.RESERVED;
    }

    public boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }

    public boolean isSameId(long id) {
        return this.id != null && this.id.equals(id);
    }

    public boolean isWaiting() {
        return this.status == ReservationStatus.WAITING;
    }

    public boolean hasSameName(String name) {
        return this.name.equals(name);
    }

    public boolean matches(String name, ReservationStatus status) {
        return this.name.equals(name) && this.status == status;
    }
}

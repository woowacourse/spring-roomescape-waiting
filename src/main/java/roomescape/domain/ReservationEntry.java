package roomescape.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ReservationEntry {

    private Long id;
    private String name;
    private Reservation reservation;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    public ReservationEntry(Long id, String name, Reservation reservation, ReservationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.reservation = reservation;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ReservationEntry reserve(String name, Reservation reservation) {
        return new ReservationEntry(null, name, reservation, ReservationStatus.RESERVED, LocalDateTime.now());
    }

    public static ReservationEntry waiting(String name, Reservation reservation) {
        return new ReservationEntry(null, name, reservation, ReservationStatus.WAITING, LocalDateTime.now());
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

    public boolean isActive() {
        return this.status != ReservationStatus.DELETED;
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

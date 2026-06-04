package roomescape.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class Reservation {

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private ReservationStatus status;
    private ReservationActiveStatus activeStatus;

    public Reservation(Long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        this(id, name, status, ReservationActiveStatus.ACTIVE, createdAt);
    }

    public Reservation(
            Long id,
            String name,
            ReservationStatus status,
            ReservationActiveStatus activeStatus,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.activeStatus = activeStatus;
        this.createdAt = createdAt;
    }

    public static Reservation reserve(String name) {
        return new Reservation(null, name, ReservationStatus.RESERVED, LocalDateTime.now());
    }

    public static Reservation waiting(String name) {
        return new Reservation(null, name, ReservationStatus.WAITING, LocalDateTime.now());
    }

    public void cancel() {
        this.activeStatus = ReservationActiveStatus.CANCELED;
    }

    public void promote() {
        this.status = ReservationStatus.RESERVED;
    }

    public boolean isSameId(long id) {
        return this.id != null && this.id.equals(id);
    }

    public boolean isActiveReserved() {
        return isActive() && isReserved();
    }

    public boolean isActiveWaiting() {
        return isActive() && isWaiting();
    }

    public boolean isActiveWithId(long id) {
        return isActive() && isSameId(id);
    }

    public boolean hasSameActiveName(String name) {
        return isActive() && hasSameName(name);
    }

    private boolean hasSameName(String name) {
        return this.name.equals(name);
    }

    private boolean isWaiting() {
        return this.status == ReservationStatus.WAITING;
    }

    private boolean isActive() {
        return this.activeStatus == ReservationActiveStatus.ACTIVE;
    }

    private boolean isReserved() {
        return this.status == ReservationStatus.RESERVED;
    }
}

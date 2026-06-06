package roomescape.domain;

import roomescape.exception.BusinessRuleViolationException;

import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationWaiting {

    private static final String OWNER_CANNOT_WAIT = "본인이 예약한 슬롯에는 대기를 신청할 수 없습니다.";

    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final Reservation reservation;

    public ReservationWaiting(
            Long id,
            String name,
            LocalDateTime createdAt,
            Reservation reservation
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.reservation = reservation;
    }

    public ReservationWaiting(
            String name,
            LocalDateTime createdAt,
            Reservation reservation
    ) {
        this(null, name, createdAt, reservation);
        if (reservation.isOwnedBy(name)) {
            throw new BusinessRuleViolationException(OWNER_CANNOT_WAIT);
        }
    }

    public boolean isOwnedBy(String name) {
        return name.equals(this.name);
    }

    public boolean isPast(LocalDateTime now) {
        return reservation.isPast(now);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Long getReservationId() {
        return reservation.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWaiting that = (ReservationWaiting) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;

public class ReservationWaiting {

    private static final String OWNER_CANNOT_WAIT = "본인이 예약한 슬롯에는 대기를 신청할 수 없습니다.";
    private static final String PAST_RESERVATION_WAITING_REJECTED = "지난 시각에는 대기할 수 없습니다.";
    private static final String NOT_OWNER = "본인의 예약 대기가 아닙니다.";

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
    }

    public static ReservationWaiting createWith(
            String name,
            LocalDateTime now,
            Reservation reservation
    ) {
        validateWaitable(name, now, reservation);
        return new ReservationWaiting(name, now, reservation);
    }

    private static void validateWaitable(String name, LocalDateTime now, Reservation reservation) {
        if (reservation.isOwnedBy(name)) {
            throw new BusinessRuleViolationException(OWNER_CANNOT_WAIT);
        }
        if (reservation.isPast(now)) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_WAITING_REJECTED);
        }
    }

    public void cancelBy(String name) {
        validateOwner(name);
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

    private void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException(NOT_OWNER);
        }
    }
}

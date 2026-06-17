package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class ReservationWaiting {
    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;
    private final ReservationSlot slot;

    public ReservationWaiting(Long id, String name, LocalDateTime createdAt, ReservationSlot slot) {
        Objects.requireNonNull(name, "예약 대기자명은 필수값 입니다.");
        Objects.requireNonNull(createdAt, "예약 대기 생성일자는 필수값 입니다.");
        Objects.requireNonNull(slot, "예약 대기 슬롯은 필수값 입니다.");

        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.slot = slot;
    }

    public static ReservationWaiting createWithoutId(String name, LocalDateTime createdAt, ReservationSlot slot) {
        return new ReservationWaiting(null, name, createdAt, slot);
    }

    public Reservation promoteToReservation() {
        return Reservation.createConfirmedWithoutId(name, slot);
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

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public LocalDate getReservationDate() {
        return slot.getDate();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ReservationWaiting reservationWaiting = (ReservationWaiting) object;
        if (id != null && reservationWaiting.id != null) {
            return Objects.equals(id, reservationWaiting.id);
        }
        return Objects.equals(name, reservationWaiting.name)
                && Objects.equals(createdAt, reservationWaiting.createdAt)
                && Objects.equals(slot, reservationWaiting.slot);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, createdAt, slot);
    }
}

package roomescape.domain.reservationWaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.slot.Slot;
import roomescape.domain.theme.Theme;
import roomescape.exception.InvalidInputException;

public class ReservationWaiting {

    private final Long id;
    private final Slot slot;
    private final String name;
    private final Long sequence;
    private final LocalDateTime createdAt;

    private ReservationWaiting(Long id, Slot slot, String name, Long sequence, LocalDateTime createdAt) {
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.sequence = sequence;
        this.createdAt = createdAt;
    }

    public static ReservationWaiting create(String name, Slot slot) {
        if (slot.isExpired()) {
            throw new InvalidInputException("이미 지난 예약에 대기열을 등록할 수 없습니다.");
        }
        return new ReservationWaiting(null, slot, name, null, LocalDateTime.now());
    }

    public static ReservationWaiting restore(Long id, Slot slot, String name, Long sequence, LocalDateTime createdAt) {
        return new ReservationWaiting(id, slot, name, sequence, createdAt);
    }

    public Reservation promote() {
        return Reservation.create(name, slot);
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

    public Long getSequence() {
        return sequence;
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

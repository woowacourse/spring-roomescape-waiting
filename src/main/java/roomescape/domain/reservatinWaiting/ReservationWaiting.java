package roomescape.domain.reservatinWaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;

import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.InvalidInputException;

public class ReservationWaiting {

    private Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime createdAt;

    public ReservationWaiting(String name, ReservationSlot slot) {
        this.name = name;
        this.slot = slot;
        this.createdAt = LocalDateTime.now();
    }

    public ReservationWaiting(Long id, String name, ReservationSlot slot, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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

    public void validatePastDateTime() {
        slot.validateNoPast();
    }

    public void validateOwner(String name) {
        if (!this.name.equals(name)) {
            throw new InvalidInputException("본인의 대기만 취소할 수 있습니다.");
        }
    }

    public Reservation promoteToReservation() {
        return new Reservation(this.name, this.slot);
    }
}

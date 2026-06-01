package roomescape.domain.reservation;

import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.InvalidInputException;

public class Reservation {

    private Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime createdAt;

    public Reservation(String name, ReservationSlot slot) {
        this.name = name;
        this.slot = slot;
        this.createdAt = LocalDateTime.now();
    }

    public Reservation(Long id, String name, ReservationSlot slot, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.createdAt = createdAt;
    }

    public Reservation withReservationId(Long id) {
        return new Reservation(id, this.name, this.slot, this.createdAt);
    }

    public Reservation withUpdatedDateAndTime(LocalDate date, ReservationTime time) {
        ReservationSlot updatedSlot = new ReservationSlot(date, time, slot.getTheme());
        return new Reservation(id, this.name, updatedSlot, this.createdAt);
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

    public void validateWaitable(String name) {
        validateDuplicatedReservationByName(name);
        validatePastDateTime();
    }

    private void validateDuplicatedReservationByName(String name) {
        if(this.name.equals(name)) {
            throw new InvalidInputException("이미 등록된 예약이 있습니다.");
        }
    }

    public void validatePastDateTime() {
        validateNoPast(this.slot.getDate(), this.slot.getTime());
    }

    public static void validateNoPast(LocalDate date, ReservationTime time) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new ExpiredDateTimeException();
        }
    }
}

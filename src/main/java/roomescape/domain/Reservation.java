package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.PAST_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.exception.RoomEscapeException;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", unique = true)
    private Slot slot;

    public Reservation(Long id, String name, Slot slot) {
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public Reservation(String name, Slot slot) {
        this(null, name, slot);
    }

    protected Reservation() {

    }

    public void verifyReservable(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점에 예약할 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public void verifyCancelableBy(String name, LocalDateTime now) {
        verifyReservedBy(name, "본인의 예약만 취소할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    public Reservation changeBy(String name, LocalDateTime now, LocalDate newDate, ReservationTime newTime) {
        verifyReservedBy(name, "본인의 예약만 변경할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 변경할 수 없습니다.");
        }

        Slot newSlot = Slot.of(newDate, newTime, slot.getTheme());

        if (newSlot.isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점으로 변경할 수 없습니다.");
        }

        return new Reservation(id, this.name, newSlot);
    }

    private void verifyReservedBy(String other, String message) {
        if (!this.name.equals(other)) {
            throw new RoomEscapeException(UNAUTHORIZED_RESERVATION, message);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Slot getSlot() {
        return slot;
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
}

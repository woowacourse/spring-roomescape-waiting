package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {

    private final Long id;
    private final User user;
    private final Slot slot;
    private final ReservationStatus status;

    public Reservation(Long id, User user, Slot slot, ReservationStatus status) {
        validate(user, slot, status);
        this.id = id;
        this.user = user;
        this.slot = slot;
        this.status = status;
    }

    public Reservation(Long id, User user, Theme theme, LocalDate date, ReservationTime time, Store store,
                       ReservationStatus status) {
        this(id, user, new Slot(null, date, theme, time, store), status);
    }

    private void validate(User user, Slot slot, ReservationStatus status) {
        if (user == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "예약자는 필수입니다.");
        }
        if (slot == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "예약 슬롯은 필수입니다.");
        }
        if (status == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "예약 상태는 필수입니다.");
        }
    }

    public boolean isInPast(LocalDateTime currentDateTime) {
        return slot.isInPast(currentDateTime);
    }

    public boolean hasSameSlot(Reservation other) {
        return slot.hasSameSlot(other.slot);
    }

    public boolean isReserved() {
        return status.equals(ReservationStatus.RESERVED);
    }

    public boolean isWaiting() {
        return status.equals(ReservationStatus.WAITING);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, user, slot, status);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Slot getSlot() {
        return slot;
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Store getStore() {
        return slot.getStore();
    }

    public ReservationStatus getStatus() {
        return status;
    }
}

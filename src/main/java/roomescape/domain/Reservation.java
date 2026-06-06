package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.PastTimeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final ReservationSlot slot;
    private final LocalDateTime createdAt;
    private final ReservationStatus status;

    public Reservation(Long id, String name, ReservationSlot slot,
                       LocalDateTime createdAt, ReservationStatus status) {

        validateNullOrBlank(name, slot, createdAt, status);
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.createdAt = createdAt;
        this.status = status;
        validateCreatable();
    }

    public Reservation(String name, ReservationSlot reservationSlot, LocalDateTime createdAt, ReservationStatus status) {
        this(null, name, reservationSlot, createdAt, status);
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

    public TimeSlot getTimeSlot() {
        return slot.getTimeSlot();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    public Reservation updateSlot(ReservationSlot updateSlot, LocalDateTime now) {
        validateNotPast(now, "이미 지난 예약은 수정할 수 없습니다.");
        validateUpdateSlot(updateSlot, now);

        if (this.slot.isSameDateAndTime(updateSlot.getDate(), updateSlot.getTimeSlot())) {
            return this;
        }
        return new Reservation(this.id, this.name, updateSlot, this.createdAt, this.status);
    }

    public Reservation promote() {
        if (isReserved()) {
            return this;
        }
        return new Reservation(this.id, this.name, this.slot, this.createdAt, ReservationStatus.RESERVED);
    }

    public void validateCancelable(LocalDateTime now) {
        validateNotPast(now, "이미 지난 예약은 삭제할 수 없습니다.");
    }

    public boolean hasSameDateAndTime(Reservation other) {
        return this.slot.isSameDateAndTime(other.getDate(), other.getTimeSlot());
    }

    public boolean hasSameSlot(Reservation other) {
        return other != null && this.slot.hasSameSlot(other.slot);
    }

    public boolean isSameReservation(Reservation other) {
        return other != null && id != null && id.equals(other.id);
    }

    public boolean isOwner(String requestName) {
        return name.equals(requestName);
    }

    public boolean isReserved() {
        return status == ReservationStatus.RESERVED;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    private void validateCreatable() {
        validateNotPast(this.createdAt, "지난 날짜/시간으로 예약하실 수 없습니다.");
    }

    private void validateNotPast(LocalDateTime baseTime, String errorMessage) {
        if (this.slot.isPast(baseTime)) {
            throw new PastTimeException(errorMessage);
        }
    }

    private void validateUpdateSlot(ReservationSlot updateSlot, LocalDateTime now) {
        if (updateSlot == null) {
            throw new IllegalArgumentException("변경할 예약 슬롯은 필수입니다.");
        }
        if (updateSlot.isPast(now)) {
            throw new PastTimeException("이미 지난 날짜로 예약을 수정할 수 없습니다.");
        }
    }

    private void validateNullOrBlank(String name, ReservationSlot reservationSlot, LocalDateTime createdAt,
                                     ReservationStatus status) {
        validateName(name);
        validateReservationSlot(reservationSlot);
        validateCreatedAt(createdAt);
        validateStatus(status);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수입니다.");
        }
    }

    private void validateReservationSlot(ReservationSlot reservationSlot) {
        if (reservationSlot == null) {
            throw new IllegalArgumentException("예약 슬롯은 필수입니다.");
        }
    }

    private void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("예약 생성 시각은 필수입니다.");
        }
    }

    private void validateStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("예약 상태는 필수입니다.");
        }
    }
}

package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.exception.PastTimeException;

public class Reservation {

    private static final int CANCEL_DEADLINE_HOURS = 24;

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

    public Reservation updateSlot(ReservationSlot updateSlot, LocalDateTime now, ReservationStatus updateStatus) {
        validateUpdatable(updateSlot, now);

        if (this.slot.isSameDateAndTime(updateSlot.getDate(), updateSlot.getTimeSlot())) {
            return this;
        }
        return new Reservation(this.id, this.name, updateSlot, this.createdAt, updateStatus);
    }

    public void validateUpdatable(ReservationSlot updateSlot, LocalDateTime now) {
        validateNotPast(now, "이미 지난 예약은 수정할 수 없습니다.");
        validateUpdateSlot(updateSlot, now);
    }

    public void validateCancelable(LocalDateTime now) {
        LocalDateTime cancelLimitTime = LocalDateTime.of(slot.getDate(), slot.getTimeSlot().getStartAt())
                .minusHours(CANCEL_DEADLINE_HOURS);

        if (!now.isBefore(cancelLimitTime)) {
            throw new PastTimeException(String.format(
                    "예약 시작 %d시간 전까지만 예약을 삭제할 수 있습니다.", CANCEL_DEADLINE_HOURS));
        }
    }

    public Reservation promote() {
        if (!isWaiting()) {
            throw new IllegalArgumentException("예약 대기만 확정 예약으로 승급할 수 있습니다.");
        }
        return new Reservation(this.id, this.name, this.slot, this.createdAt, ReservationStatus.RESERVED);
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
            throw new IllegalArgumentException("변경할 날짜와 시간은 필수입니다.");
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

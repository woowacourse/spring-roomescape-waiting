package roomescape.reservation.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.slot.domain.Slot;

@Getter
public class Reservation {
    private final Long id;
    private final Long memberId;
    private final Slot slot;
    private final ReservationStatus status;
    private final String orderId;
    private final String idempotencyKey;
    private final int amount;
    private final String paymentKey;

    private Reservation(
            Long id,
            Long memberId,
            Slot slot,
            ReservationStatus status,
            String orderId,
            String idempotencyKey,
            int amount,
            String paymentKey
    ) {
        this.id = id;
        this.memberId = Objects.requireNonNull(memberId, "memberId는 null일 수 없습니다.");
        this.slot = Objects.requireNonNull(slot, "slot은 null일 수 없습니다.");
        this.status = Objects.requireNonNull(status, "status는 null일 수 없습니다.");
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }

    public static Reservation create(long memberId, Slot slot) {
        return createConfirmed(memberId, slot);
    }

    public static Reservation createPending(long memberId, Slot slot, String orderId, String idempotencyKey) {
        return new Reservation(
                null,
                memberId,
                slot,
                ReservationStatus.PENDING,
                orderId,
                idempotencyKey,
                slot.getPrice(),
                null
        );
    }

    public static Reservation createConfirmed(long memberId, Slot slot) {
        return new Reservation(null, memberId, slot, ReservationStatus.CONFIRMED, null, null, slot.getPrice(), null);
    }

    public static Reservation of(Long id, Long memberId, Slot slot) {
        return new Reservation(id, memberId, slot, ReservationStatus.CONFIRMED, null, null, slot.getPrice(), null);
    }

    public static Reservation of(
            Long id,
            Long memberId,
            Slot slot,
            ReservationStatus status,
            String orderId,
            String idempotencyKey,
            int amount,
            String paymentKey
    ) {
        return new Reservation(id, memberId, slot, status, orderId, idempotencyKey, amount, paymentKey);
    }

    public boolean isPending() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.PAYMENT_CHECK_REQUIRED;
    }

    public boolean isConfirmedWith(String paymentKey) {
        return status == ReservationStatus.CONFIRMED && Objects.equals(this.paymentKey, paymentKey);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(this.memberId, memberId);
    }

    public void validateOwnedBy(Long memberId) {
        if (!isOwnedBy(memberId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_OWNED_BY_MEMBER, id);
        }
    }

    public void validateNotPast(LocalDateTime now) {
        slot.validateNotPast(now);
    }

    public void validateCancelable(LocalDateTime now) {
        validateNotPast(now);
    }

    public Long getSlotId() {
        return slot.getId();
    }

}

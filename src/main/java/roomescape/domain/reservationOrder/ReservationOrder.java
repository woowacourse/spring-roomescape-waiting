package roomescape.domain.reservationOrder;

import java.util.UUID;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.PaymentAmountMismatchException;

public class ReservationOrder {

    private final String id;
    private final long amount;
    private final String paymentKey;
    private final long reservationId;
    private final OrderStatus status;

    private ReservationOrder(String id, long amount, String paymentKey, long reservationId, OrderStatus status) {
        this.id = id;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
        this.status = status;
    }

    public static ReservationOrder create(long amount, long reservationId) {
        return new ReservationOrder(UUID.randomUUID().toString(), amount, null, reservationId, OrderStatus.PENDING);
    }

    public static ReservationOrder restore(String id, long amount, String paymentKey, long reservationId) {
        return new ReservationOrder(id, amount, paymentKey, reservationId, deriveStatus(paymentKey));
    }

    public static ReservationOrder restore(String id, long amount, String paymentKey, long reservationId, OrderStatus status) {
        return new ReservationOrder(id, amount, paymentKey, reservationId, status);
    }

    public ReservationOrder update(String paymentKey) {
        return new ReservationOrder(this.id, this.amount, paymentKey, this.reservationId, deriveStatus(paymentKey));
    }

    public void verifyAmount(long requestedAmount) {
        if (this.amount != requestedAmount) {
            throw new PaymentAmountMismatchException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }
    }

    public ReservationOrder confirm(String paymentKey) {
        if (isConfirmed()) {
            throw new AlreadyProcessedException("이미 확정된 주문입니다.");
        }
        return new ReservationOrder(this.id, this.amount, paymentKey, this.reservationId, OrderStatus.CONFIRMED);
    }

    /**
     * read timeout 등으로 승인 결과가 불명확한 상태. "결제 실패"로 단정하지 않고, 멱등키로 안전하게 재시도할 수 있다.
     */
    public ReservationOrder markUnknown() {
        if (isConfirmed()) {
            return this;
        }
        return new ReservationOrder(this.id, this.amount, this.paymentKey, this.reservationId, OrderStatus.UNKNOWN);
    }

    public boolean isConfirmed() {
        return this.paymentKey != null;
    }

    private static OrderStatus deriveStatus(String paymentKey) {
        if (paymentKey != null) {
            return OrderStatus.CONFIRMED;
        }
        return OrderStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public long getAmount() {
        return amount;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public long getReservationId() {
        return reservationId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}

package roomescape.domain.reservationOrder;

import java.util.UUID;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.PaymentAmountMismatchException;

public class ReservationOrder {

    private final String id;
    private final long amount;
    private String paymentKey;
    private final long reservationId;

    private ReservationOrder(String id, long amount, String paymentKey, long reservationId) {
        this.id = id;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
    }

    public static ReservationOrder create(long amount, long reservationId) {
        return new ReservationOrder(UUID.randomUUID().toString(), amount, null, reservationId);
    }

    public static ReservationOrder restore(String id, long amount, String paymentKey, long reservationId) {
        return new ReservationOrder(id, amount, paymentKey, reservationId);
    }

    public ReservationOrder update(String paymentKey) {
        return new ReservationOrder(this.id, this.amount, paymentKey, this.reservationId);
    }

    // 금액 위변조 검증은 주문 자신이 책임진다
    public void verifyAmount(long requestedAmount) {
        if (this.amount != requestedAmount) {
            throw new PaymentAmountMismatchException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }
    }

    // paymentKey 부여(결제 확정) 상태 전이
    public ReservationOrder confirm(String paymentKey) {
        if (isConfirmed()) {
            throw new AlreadyProcessedException("이미 확정된 주문입니다.");
        }
        return new ReservationOrder(this.id, this.amount, paymentKey, this.reservationId);
    }

    public boolean isConfirmed() {
        return this.paymentKey != null;
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
}

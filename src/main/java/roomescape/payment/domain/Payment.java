package roomescape.payment.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.payment.exception.PaymentAmountMismatchException;

import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    private final Long id;
    private final Long reservationId;
    private final Long slotId;
    private final String orderId;
    private String paymentKey;
    private final Long amount;
    private PaymentStatus status;

    public static Payment pending(Long reservationId, Long slotId, String orderId, Long amount) {
        return new Payment(null, reservationId, slotId, orderId, null, amount, PaymentStatus.PENDING);
    }

    public static Payment load(Long id, Long reservationId, Long slotId, String orderId, String paymentKey, Long amount, PaymentStatus status) {
        return new Payment(id, reservationId, slotId, orderId, paymentKey, amount, status);
    }

    public void validateAmountMatch(Long requestAmount) {
        if (!Objects.equals(amount, requestAmount)) {
            throw new PaymentAmountMismatchException(amount, requestAmount);
        }
    }

    public void confirm(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.CONFIRMED;
    }

    public void fail() {
        updateStatus(PaymentStatus.FAILED);
    }

    public void unknown() {
        updateStatus(PaymentStatus.UNKNOWN);
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }

}

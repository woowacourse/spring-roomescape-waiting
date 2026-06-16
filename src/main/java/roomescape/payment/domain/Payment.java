package roomescape.payment.domain;

import java.util.regex.Pattern;

public class Payment {

    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{6,64}$");

    private final Long id;
    private final Long reservationId;
    private final String orderId;
    private final String paymentKey;
    private final Long amount;
    private final PaymentState state;

    private Payment(Long id, Long reservationId, String orderId, String paymentKey, Long amount, PaymentState state) {
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 정보는 필수입니다.");
        }
        if (orderId == null || !ORDER_ID_PATTERN.matcher(orderId).matches()) {
            throw new IllegalArgumentException("주문 번호는 6~64자의 영숫자/-/_ 형식이어야 합니다: " + orderId);
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 양수여야 합니다.");
        }
        if (state == null) {
            throw new IllegalArgumentException("결제 상태는 필수입니다.");
        }
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.state = state;
    }

    public static Payment pending(Long reservationId, String orderId, Long amount) {
        return new Payment(null, reservationId, orderId, null, amount, PaymentState.PENDING);
    }

    public static Payment restore(Long id, Long reservationId, String orderId, String paymentKey, Long amount,
                                  PaymentState state) {
        return new Payment(id, reservationId, orderId, paymentKey, amount, state);
    }

    public Payment confirm(String paymentKey) {
        return new Payment(id, reservationId, orderId, paymentKey, amount, PaymentState.CONFIRMED);
    }

    public boolean isAmountMismatched(Long requestedAmount) {
        return !amount.equals(requestedAmount);
    }

    public boolean isPending() {
        return state == PaymentState.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentState getState() {
        return state;
    }
}
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
    private final String idempotencyKey;

    private Payment(Long id, Long reservationId, String orderId, String paymentKey, Long amount, PaymentState state,
                    String idempotencyKey) {
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
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 300) {
            throw new IllegalArgumentException("멱등키는 1~300자여야 합니다.");
        }
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.state = state;
        this.idempotencyKey = idempotencyKey;
    }

    public static Payment pending(Long reservationId, String orderId, Long amount, String idempotencyKey) {
        return new Payment(null, reservationId, orderId, null, amount, PaymentState.PENDING, idempotencyKey);
    }

    public static Payment restore(Long id, Long reservationId, String orderId, String paymentKey, Long amount,
                                  PaymentState state, String idempotencyKey) {
        return new Payment(id, reservationId, orderId, paymentKey, amount, state, idempotencyKey);
    }

    public Payment confirm(String paymentKey) {
        return new Payment(id, reservationId, orderId, paymentKey, amount, PaymentState.CONFIRMED, idempotencyKey);
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
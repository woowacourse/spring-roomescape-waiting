package roomescape.payment.domain;

public class Payment {
    private final String orderId;
    private final Long amount;
    private final String paymentKey;
    private final Long reservationId;

    private Payment(String orderId, Long amount, String paymentKey, Long reservationId) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
    }

    public static Payment restore(String orderId, Long amount, String paymentKey, Long reservationId) {
        return new Payment(orderId, amount, paymentKey, reservationId);
    }

    // 결제 정보 생성(이후 토스 결제창 뜸)
    public static Payment pending(String orderId, Long amount, Long reservationId) {
        return new Payment(orderId, amount, null, reservationId);
    }

    // 결제 완료해서 새로운 paymentKey 발급 받음
    public Payment confirm(String paymentKey) {
        return new Payment(orderId, amount, paymentKey, reservationId);
    }

    public Long getAmount() {
        return amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getReservationId() {
        return reservationId;
    }
}

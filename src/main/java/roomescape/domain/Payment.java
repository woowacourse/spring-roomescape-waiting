package roomescape.domain;

public class Payment {
    private final Long id;
    private final Long paymentOrderId;
    private final String paymentKey;
    private final Long amount;

    public static Payment create(Long paymentOrderId, String paymentKey, Long amount) {
        return new Payment(null, paymentOrderId, paymentKey, amount);
    }

    public Payment(Long id, Long paymentOrderId, String paymentKey, Long amount) {
        this.id = id;
        this.paymentOrderId = paymentOrderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Long getPaymentOrderId() {
        return paymentOrderId;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getAmount() {
        return amount;
    }
}

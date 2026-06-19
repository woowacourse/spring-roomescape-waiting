package roomescape.domain.payment;

public interface PaymentGateway {

    PaymentResult confirm(String paymentKey, String orderId, long amount);
}

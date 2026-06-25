package roomescape.payment;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);

    void cancel(String paymentKey, String reason);
}

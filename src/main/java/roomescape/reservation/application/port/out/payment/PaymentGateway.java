package roomescape.reservation.application.port.out.payment;

public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);

    PaymentResult findByPaymentKey(String paymentKey);

    PaymentResult findByOrderId(String orderId);
}

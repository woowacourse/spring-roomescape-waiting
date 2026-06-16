package roomescape.reservation.application.port.out.payment;

public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);
}

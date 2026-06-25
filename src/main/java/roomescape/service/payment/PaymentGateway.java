package roomescape.service.payment;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

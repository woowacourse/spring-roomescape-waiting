package roomescape.payment;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

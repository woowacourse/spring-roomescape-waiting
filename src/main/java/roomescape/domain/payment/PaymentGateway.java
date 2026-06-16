package roomescape.domain.payment;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}
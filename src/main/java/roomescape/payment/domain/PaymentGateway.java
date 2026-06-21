package roomescape.payment.domain;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);

    String clientKey();
}

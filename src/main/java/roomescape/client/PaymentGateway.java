package roomescape.client;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

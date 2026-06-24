package roomescape.domain;

public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);
}

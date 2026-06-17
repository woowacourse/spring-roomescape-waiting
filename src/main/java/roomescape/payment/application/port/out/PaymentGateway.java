package roomescape.payment.application.port.out;

import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

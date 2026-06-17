package roomescape.service.payment.port;

import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

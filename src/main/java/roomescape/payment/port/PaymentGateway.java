package roomescape.payment.port;

import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);

}
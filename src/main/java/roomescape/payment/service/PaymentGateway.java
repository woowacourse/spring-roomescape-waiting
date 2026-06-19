package roomescape.payment.service;

import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

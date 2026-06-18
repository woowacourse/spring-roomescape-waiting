package roomescape.service;

import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;

public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);
}

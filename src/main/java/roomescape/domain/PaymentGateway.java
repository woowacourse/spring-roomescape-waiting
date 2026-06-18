package roomescape.domain;

import roomescape.dto.payment.PaymentConfirmation;
import roomescape.dto.payment.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);

}

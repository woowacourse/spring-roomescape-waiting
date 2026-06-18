package roomescape.payment.client.gateway;

import roomescape.payment.service.dto.PaymentConfirmation;
import roomescape.payment.service.dto.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);

}

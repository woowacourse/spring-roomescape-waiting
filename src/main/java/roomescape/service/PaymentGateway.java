package roomescape.service;

import roomescape.client.dto.PaymentConfirmation;
import roomescape.domain.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

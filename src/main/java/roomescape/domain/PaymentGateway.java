package roomescape.domain;

import roomescape.dto.request.PaymentConfirmation;
import roomescape.dto.response.PaymentResult;

public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

package roomescape.service.port;

import roomescape.domain.vo.PaymentConfirmation;
import roomescape.domain.vo.PaymentResult;

public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);
}

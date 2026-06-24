package roomescape.domain.payment;

import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;

public interface PaymentClient {

    PaymentConfirmResponse confirm(PaymentConfirmRequest request);
}

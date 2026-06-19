package roomescape.payment.application.port.in;

import roomescape.payment.application.dto.request.PaymentConfirmRequest;
import roomescape.payment.application.dto.response.PaymentConfirmResponse;

public interface ConfirmPaymentUseCase {

    PaymentConfirmResponse confirm(PaymentConfirmRequest request, long memberId);
}

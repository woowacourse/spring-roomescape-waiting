package roomescape.payment.application.port.in;

import roomescape.payment.application.dto.request.PaymentFailRequest;

public interface HandlePaymentFailureUseCase {

    void handleFailure(PaymentFailRequest request, long memberId);
}

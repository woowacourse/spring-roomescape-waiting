package roomescape.payment.application;

import roomescape.payment.application.dto.PaymentCancel;
import roomescape.payment.application.dto.PaymentConfirmation;
import roomescape.payment.application.dto.PaymentResult;

public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);
    PaymentResult cancel(PaymentCancel cancel);
}

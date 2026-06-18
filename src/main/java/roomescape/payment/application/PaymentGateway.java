package roomescape.payment.application;

import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;

/**
 * 결제 게이트웨이 포트. 애플리케이션 계층은 이 포트에만 의존하고, 구체적인 PG(Toss 등)는 어댑터가 구현한다.
 */
public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

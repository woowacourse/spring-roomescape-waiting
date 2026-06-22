package roomescape.domain.payment;

/**
 * 결제 승인을 외부 PG사에 위임하는 포트. 구현체(어댑터)만 특정 PG사를 알고, 도메인/애플리케이션은 이 인터페이스만 의존한다. PG사를 바꿔도 이 계약은 그대로다.
 */
public interface PaymentGateway {

    PaymentResult confirm(PaymentConfirmation confirmation);
}

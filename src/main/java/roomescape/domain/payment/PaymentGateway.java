package roomescape.domain.payment;

/**
 * 결제 승인 포트. PaymentService가 의존하는 유일한 결제 추상이며, 토스를 모른다.
 * 구현체(TossPaymentGateway)는 인프라에 살고, PG사를 바꿔도 이 포트는 그대로다.
 */
public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);

    /**
     * 프론트 결제창 초기화에 쓰는 공개 클라이언트 키. 비밀이 아니며, 포트를 통해서만 노출해
     * 웹 계층이 토스 어댑터를 직접 알지 않게 한다.
     */
    String clientKey();
}

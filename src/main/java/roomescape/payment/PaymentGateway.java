package roomescape.payment;

/**
 * 결제 승인 포트. PaymentService가 의존하는 유일한 결제 추상이며, 토스를 모른다.
 * 구현체(TossPaymentGateway)는 인프라에 살고, PG사를 바꿔도 이 포트는 그대로다.
 */
public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);

    /**
     * 주문에 대한 결제가 게이트웨이에서 승인 완료(DONE)됐는지 조회한다(읽기). 응답 유실로 결과가 불명확해진
     * 주문을, 게이트웨이를 진실의 원천 삼아 확정/실패로 수렴시키는 reconciliation에서 쓴다.
     */
    PaymentApprovalStatus findStatus(String orderId);

    /**
     * 프론트 결제창 초기화에 쓰는 공개 클라이언트 키. 비밀이 아니며, 포트를 통해서만 노출해
     * 웹 계층이 토스 어댑터를 직접 알지 않게 한다.
     */
    String clientKey();
}

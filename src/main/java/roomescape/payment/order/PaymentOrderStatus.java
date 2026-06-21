package roomescape.payment.order;

/**
 * 주문(결제)의 진행 상태. 토스의 결제 상태(DONE/CANCELED...)와 달리, 우리 서버 관점에서
 * "이 주문의 결제가 어디까지 왔는가"를 표현한다.
 */
public enum PaymentOrderStatus {

    /** 주문은 만들어졌으나 아직 승인 전. 연결 실패(요청 미도달)도 재시도 가능하므로 여기에 머문다. */
    PENDING("결제 대기"),
    /** 토스 승인 성공. paymentKey/approvedAmount 가 채워진다. */
    CONFIRMED("확정"),
    /** 토스가 명확히 거절. */
    FAILED("실패"),
    /** read timeout 등으로 승인 여부 불명. "실패"로 단정하지 않고 재확인이 필요한 상태. */
    UNKNOWN("확인 필요"),
    ;

    private final String label;

    PaymentOrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

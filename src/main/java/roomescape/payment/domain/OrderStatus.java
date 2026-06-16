package roomescape.payment.domain;

import roomescape.payment.infra.client.PaymentStatus;

public enum OrderStatus {
    PENDING("결제 대기"),    // 사용자가 결제창을 열었거나 입금을 기다리는 상태
    COMPLETED("결제 완료"),  // 토스에서 DONE이 떨어져 예약이 최종 확정된 상태
    FAILED("결제 실패"),     // 시간 초과, 승인 거절 등으로 결제가 실패한 상태
    CANCELED("결제 취소");   // 결제 완료 후, 사용자가 예약을 취소해서 환불된 상태

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public static OrderStatus fromToss(PaymentStatus tossStatus) {
        return switch (tossStatus) {
            case DONE -> COMPLETED;
            case CANCELED, PARTIAL_CANCELED -> CANCELED;
            case ABORTED, EXPIRED, UNKNOWN -> FAILED;
            case READY, IN_PROGRESS, WAITING_FOR_DEPOSIT -> PENDING;
        };
    }
}

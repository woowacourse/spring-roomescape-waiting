package roomescape.payment;

/**
 * 결제 주문의 우리 쪽 도메인 상태. 토스 응답 status를 그대로 옮기는 {@link PaymentStatus}와는 별개로,
 * 내역 페이지에 보여줄 "대기/확정/확인 필요"를 구분하기 위해 존재한다.
 */
public enum PaymentOrderStatus {
    PENDING,
    CONFIRMED,
    NEEDS_CONFIRMATION
}

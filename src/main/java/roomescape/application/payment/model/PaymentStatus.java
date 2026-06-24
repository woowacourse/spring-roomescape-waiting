package roomescape.application.payment.model;

/**
 * 결제 상태를 표현하는 도메인 enum. 정의되지 않은 외부 상태값은 UNKNOWN 으로 떨어뜨린다.
 */
public enum PaymentStatus {

    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
    UNKNOWN;

    public static PaymentStatus from(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}

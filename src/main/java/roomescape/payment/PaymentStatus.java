package roomescape.payment;

/**
 * 결제 상태를 표현하는 도메인 enum. 정의되지 않은 외부 상태값은 UNKNOWN 으로 떨어뜨린다.
 */
public enum PaymentStatus {

    READY("결제 대기"),
    IN_PROGRESS("결제 대기"),
    WAITING_FOR_DEPOSIT("결제 대기"),
    DONE("확정"),
    FAILED("실패"),
    CANCELED("실패"),
    PARTIAL_CANCELED("실패"),
    ABORTED("실패"),
    EXPIRED("실패"),
    UNKNOWN("확인 필요");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

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

    public String getDescription() {
        return description;
    }
}

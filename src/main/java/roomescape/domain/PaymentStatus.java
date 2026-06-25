package roomescape.domain;

public enum PaymentStatus {
    READY,
    FAILED,
    CANCELED,
    CHECK_REQUIRED,
    CONFIRMED;

    public static PaymentStatus fromTossStatus(String tossStatus) {
        if ("DONE".equals(tossStatus)) {
            return CONFIRMED;
        }
        throw new IllegalArgumentException("지원하지 않는 Toss 결제 상태입니다: " + tossStatus);
    }
}

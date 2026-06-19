package roomescape.feature.payment;

import java.util.Arrays;
import java.util.Set;

/**
 * 토스 결제 승인 API의 에러 코드를 도메인 처리 정책 단위로 분류한다.
 * 토스 에러 코드는 90개 이상이고 토스가 소유·확장하는 개방 집합이므로,
 * 도메인이 실제로 다르게 처리할 코드만 큐레이션하고 나머지는 UNKNOWN 으로 흡수한다.
 * (valueOf 처럼 미지의 코드에 예외를 던지지 않는다.)
 */
public enum PaymentFailureType {

    ALREADY_DONE(Set.of(
            "ALREADY_COMPLETED_PAYMENT",
            "ALREADY_PROCESSED_PAYMENT",
            "DUPLICATED_REQUEST"
    )),
    RETRYABLE(Set.of(
            "COMMON_ERROR",
            "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING",
            "FAILED_INTERNAL_SYSTEM_PROCESSING",
            "UNKNOWN_ERROR"
    )),
    CARD_DECLINED(Set.of(
            "INVALID_REJECT_CARD",
            "REJECT_CARD_COMPANY",
            "EXCEED_MAX_PAYMENT_AMOUNT",
            "REJECT_ACCOUNT_PAYMENT"
    )),
    CLIENT_FAULT(Set.of(
            "INVALID_API_KEY",
            "UNAUTHORIZED_KEY",
            "INVALID_REQUEST",
            "INVALID_PAYMENT_KEY"
    )),
    UNKNOWN(Set.of());

    private final Set<String> codes;

    PaymentFailureType(Set<String> codes) {
        this.codes = codes;
    }

    public static PaymentFailureType from(String code) {
        return Arrays.stream(values())
                .filter(type -> type.codes.contains(code))
                .findFirst()
                .orElse(UNKNOWN);
    }
}

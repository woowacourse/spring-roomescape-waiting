package roomescape.domain.history;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 결제 승인 시도 한 건의 결과(결제 내역 한 줄). 승인뿐 아니라 실패도 함께 남긴다.
 */
public record PaymentHistory(
        String at,            // 발생 시각(HH:mm:ss) — 템플릿에서 바로 출력하려고 문자열로 보관한다.
        String orderId,
        Long amount,
        String paymentKey,
        String status,
        String detail,
        boolean success
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static PaymentHistory of(
            boolean success, String orderId, Long amount, String paymentKey, String status, String detail) {
        return new PaymentHistory(
                LocalTime.now().format(FORMATTER), orderId, amount, paymentKey, status, detail, success);
    }

}

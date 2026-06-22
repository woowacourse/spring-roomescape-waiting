package roomescape.domain.payment;

/**
 * 결제 승인 결과. 게이트웨이가 외부 응답을 번역해 돌려주는 출력 모델.
 */
public record PaymentResult(String paymentKey, String orderId, PaymentStatus status, long approvedAmount) {

    public boolean isSuccess() {
        return status.isSuccess();
    }
}

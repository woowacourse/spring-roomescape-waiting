package roomescape.exception;

import roomescape.common.BusinessException;

public class PaymentException extends BusinessException {

    public PaymentException(String message) {
        super(message);
    }

    public static class AmountMismatch extends PaymentException {

        public AmountMismatch(long expected, long actual) {
            super("결제 금액이 일치하지 않습니다. 저장된 금액=" + expected + ", 요청 금액=" + actual);
        }
    }

    public static class CheckRequired extends PaymentException {

        public CheckRequired() {
            super("결제 승인 결과를 확인해야 합니다. 결제가 완료되었을 수 있으니 잠시 후 다시 확인하거나 재시도해주세요.");
        }
    }

    public static class Rejected extends PaymentException {

        public Rejected(String message) {
            super(message);
        }
    }
}

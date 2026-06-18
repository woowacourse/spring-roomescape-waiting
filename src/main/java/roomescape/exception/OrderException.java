package roomescape.exception;

import roomescape.common.BusinessException;

public class OrderException extends BusinessException {

    public OrderException(String message) {
        super(message);
    }

    public static class AlreadyProcessed extends OrderException {

        public AlreadyProcessed() {
            super("이미 처리된 주문입니다.");
        }
    }
}

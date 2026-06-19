package roomescape.feature.payment;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final PaymentFailureType failureType;
    private final String code;

    public PaymentException(PaymentFailureType failureType, String code, String message) {
        super(message);

        this.failureType = failureType;
        this.code = code;
    }
}

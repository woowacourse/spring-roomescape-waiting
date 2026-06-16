package roomescape.payment;

import org.springframework.http.HttpStatusCode;

public abstract class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }

    public abstract HttpStatusCode getStatus();

    public abstract String getCode();
}

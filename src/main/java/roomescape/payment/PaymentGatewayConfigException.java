package roomescape.payment;

import org.springframework.http.HttpStatusCode;

public class PaymentGatewayConfigException extends PaymentGatewayException {

    private final HttpStatusCode status;
    private final String code;

    public PaymentGatewayConfigException(HttpStatusCode status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    @Override
    public HttpStatusCode getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }
}

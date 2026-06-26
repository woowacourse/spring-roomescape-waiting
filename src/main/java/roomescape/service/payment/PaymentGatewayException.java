package roomescape.service.payment;

public class PaymentGatewayException extends RuntimeException {

    private final PaymentFailureCategory failureCategory;
    private final String code;

    public PaymentGatewayException(PaymentFailureCategory failureCategory, String code, String message) {
        super(message);
        this.failureCategory = failureCategory;
        this.code = code;
    }

    public PaymentFailureCategory getFailureCategory() {
        return failureCategory;
    }

    public String getCode() {
        return code;
    }

    public boolean isDefinitiveFailure() {
        return failureCategory == PaymentFailureCategory.DEFINITIVE;
    }

    public boolean requiresConfirmationCheck() {
        return failureCategory == PaymentFailureCategory.CONFIRMATION_UNKNOWN;
    }
}

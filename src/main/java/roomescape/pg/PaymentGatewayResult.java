package roomescape.pg;

public sealed interface PaymentGatewayResult {

    record Approved(PaymentResult payment) implements PaymentGatewayResult {
    }

    record Unknown(String message) implements PaymentGatewayResult {
    }

    record Rejected(String code, String message) implements PaymentGatewayResult {
    }
}

package roomescape.payment.exception;

public class PaymentGatewayConnectionException extends RuntimeException {

  public PaymentGatewayConnectionException(String message) {
    super(message);
  }
}

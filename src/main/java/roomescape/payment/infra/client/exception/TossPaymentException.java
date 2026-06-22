package roomescape.payment.infra.client.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public abstract class TossPaymentException extends RuntimeException {

  private final HttpStatusCode status;
  private final String code;

  public TossPaymentException(HttpStatusCode status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

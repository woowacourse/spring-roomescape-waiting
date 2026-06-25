package roomescape.common.exception;

public class OutboundRateLimitException extends RuntimeException {

  public OutboundRateLimitException(String message) {
    super(message);
  }

}
